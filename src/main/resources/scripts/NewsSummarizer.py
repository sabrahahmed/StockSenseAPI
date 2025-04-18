#!/usr/bin/env python3
import sys
import yfinance as yf
import random
import requests
from bs4 import BeautifulSoup
import json
from groq import Groq
import time
import re
import signal

# Backend URL is not used in this streaming model.
# backend_url = "http://localhost:8080/api/news/batch"

client = Groq(api_key="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")

USER_AGENTS = [
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36",
    "Mozilla/5.0 (iPhone; CPU iPhone OS 15_2 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) Version/15.2 Mobile/15E148 Safari/537.36",
]

def timeout_handler(signum, frame):
    raise TimeoutError("Article processing took too long (>2s).")

signal.signal(signal.SIGALRM, timeout_handler)

def scrape_article(link):
    try:
        headers = {
            "User-Agent": random.choice(USER_AGENTS),
            "Referer": "https://www.google.com/",
            "Accept-Language": "en-US,en;q=0.9",
        }
        resp = requests.get(link, headers=headers, timeout=10)
        resp.raise_for_status()
        soup = BeautifulSoup(resp.text, "html.parser")
        paragraphs = soup.find_all("p")
        return "\n".join(p.get_text(strip=True) for p in paragraphs)
    except requests.RequestException:
        return None

def AI_Tools(link, ticker_symbol):
    signal.alarm(2)
    try:
        article_text = scrape_article(link)
        if not article_text:
            return {"error": "Scraping failed or empty text."}

        prompt = f"""
        IMPORTANT: Return your response in a single code block with triple backticks, like:

        ```json
        {{
            "summary": [
                "Key point 1 from the article.",
                "Key point 2 from the article.",
                "Key point 3 from the article.",
                "Key point 4 from the article.",
                "Key point 5 from the article."
            ],
            "score": "50",
            "sentiment": "short-term bullish",
            "sentiment_explanation": "Insert 200 word explanation here for why the sentiment was assigned"
        }}
        ```

        No additional commentary or explanations.

        Your JSON must have exactly these keys:
        - "summary": (array of 5 concise bullet points)
        - "score": (a number between 1–100, no % or $)
        - "sentiment": (one of: short-term bullish, long-term bullish, short-term bearish, long-term bearish, neutral)
        - "sentiment_explanation": (200-word explanation for sentiment classification)

        Classify sentiment for the {ticker_symbol} stock **only** based on this article:

        {article_text}
        """

        chat_resp = client.chat.completions.create(
            messages=[{"role": "user", "content": prompt}],
            model="llama3-8b-8192",
        )

        llm_output = chat_resp.choices[0].message.content.strip()

        match = re.search(r"```(?:json)?\s*(\{[\s\S]*?\})\s*```", llm_output) or re.search(r"(\{[\s\S]*?\})", llm_output)
        if not match:
            return {"error": "No valid JSON object found"}

        raw_json = match.group(1)
        if raw_json.count('{') > raw_json.count('}'):
            raw_json += '}' * (raw_json.count('{') - raw_json.count('}'))

        ai_data = json.loads(raw_json)

        if not all(k in ai_data for k in ("summary", "score", "sentiment", "sentiment_explanation")):
            return {"error": "Missing required fields"}

        content_words = len(article_text.split())
        summary_words = len(" ".join(ai_data["summary"]).split())
        time_saved = round((content_words - summary_words) / 200, 2)

        return {
            "summary": ai_data["summary"],
            "score": ai_data["score"],
            "sentiment": ai_data["sentiment"],
            "sentimentExplanation": ai_data["sentiment_explanation"],
            "timeSaved": time_saved,
        }

    except Exception as e:
        return {"error": str(e)}
    finally:
        signal.alarm(0)


if __name__ == "__main__":
    ticker_symbol = sys.argv[1]
    fetch_count = 50

    ticker = yf.Ticker(ticker_symbol)
    raw_news = ticker.get_news(count=fetch_count)

    articles_info = []
    for article in raw_news:
        try:
            articles_info.append({
                "title": article["content"]["title"],
                "link": article["content"]["canonicalUrl"]["url"],
                "publishedAt": article["content"]["pubDate"]
            })
        except (KeyError, TypeError):
            continue

    batch_size = 1
    results_batch = []
    count = 0
    REQUIRED = 15

    for article_info in articles_info:
        if count >= REQUIRED:
            break

        result = AI_Tools(article_info["link"], ticker_symbol)
        if "error" in result or "summary" not in result:
            continue

        results_batch.append({
            "title": article_info["title"],
            "link": article_info["link"],
            "publishedAt": article_info["publishedAt"],
            "summary": result["summary"],
            "score": result["score"],
            "sentiment": result["sentiment"],
            "explanation": result["sentimentExplanation"],
            "timeSaved": result["timeSaved"]
        })
        count += 1

        if len(results_batch) == batch_size:
            # Print the current batch as a JSON array followed by a newline
            sys.stdout.write(json.dumps(results_batch) + "\n")
            sys.stdout.flush()
            results_batch = []  # Reset batch

    # Send any remaining articles that didn't complete a full batch
    if results_batch:
        sys.stdout.write(json.dumps(results_batch) + "\n")
        sys.stdout.flush()
