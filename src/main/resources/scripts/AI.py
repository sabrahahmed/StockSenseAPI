import random
import sys
import requests
from bs4 import BeautifulSoup
import json
from groq import Groq

# Initialize the Groq client
client = Groq(
    api_key="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",  # API Key
)

# List of real browser user-agents
USER_AGENTS = [
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36",
    "Mozilla/5.0 (iPhone; CPU iPhone OS 15_2 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) Version/15.2 Mobile/15E148 Safari/537.36",
]

def scrape_article(link):
    """Scrapes an article from a given link and returns its full text."""
    try:
        headers = {
            "User-Agent": random.choice(USER_AGENTS),
            "Referer": "https://www.google.com/",
            "Accept-Language": "en-US,en;q=0.9",
        }

        # time.sleep(5)  # Avoid rate limits
        article_response = requests.get(link, headers=headers, timeout=10)
        article_response.raise_for_status()

        article_soup = BeautifulSoup(article_response.text, 'html.parser')
        paragraphs = article_soup.find_all('p')
        full_content = '\n'.join(p.text.strip() for p in paragraphs)
        return full_content if full_content else "No content found in the article."

    except requests.exceptions.RequestException as e:
        print(f"Error fetching article: {e}")
        return None

def AI_Tools(link):
    full_content = scrape_article(link)

    # Calculate word count
    content_word_count = len(full_content.split())

    prompt = f"""
        You must return a valid JSON object ONLY. Do not include explanations, preambles, or additional text.
        
        Return a response in this exact JSON structure:
        
        {{
            "summary": [
                "Key point 1 from the article.",
                "Key point 2 from the article.",
                "Key point 3 from the article.",
                "Key point 4 from the article.",
                "Key point 5 from the article."
            ],
            "sentimentScore": "Sentiment score on a scale of 1 to 100, strictly a number with no symbols or words."
        }}
        
        The summary should:
        - Be **concise and factual** bullet points.
        - **Focus only on the most relevant points** directly derived from the article.
        - **Avoid using introductory phrases** such as "The article discusses" or "According to the article".
        - Be **neutral, clear, and objective**, ensuring that it doesn’t reflect bias or personal opinions.
        
        The sentiment score must:
        - Be a **single number** between **1 and 100**, without any symbols, words, or additional formatting.
        - **Reflect the overall sentiment** of the article: 
            - A score closer to 1 represents a negative sentiment.
            - A score closer to 100 represents a positive sentiment.
            - A score of 50 represents a neutral sentiment.
        
        **Do not include extraneous information**. Ignore any advertisements, irrelevant content, or non-article sections. Here is the article:\n\n{full_content}
        """

    chat_completion = client.chat.completions.create(
        messages=[
            {
                "role": "user",
                "content": prompt
            }
        ],
        model="llama3-8b-8192",
    )

    # Get the response from the model
    response = chat_completion.choices[0].message.content.strip()

    try:
        result = json.loads(response)
    except json.JSONDecodeError:
        return {"error": "Failed to parse response as JSON"}

    # Calculate summary word count
    summary_word_count = len(" ".join(result["summary"]).split())

    # Calculate reading time (assuming average reading speed of 200 WPM)
    reading_time = round(content_word_count / 200, 2)

    # Calculate summarized reading time
    summarized_time = round(summary_word_count / 200, 2)

    # Calculate time saved
    time_saved = round(reading_time - summarized_time, 2)

    # Return final JSON object
    final_result = {
        "summary": result["summary"],
        "sentimentScore": result["sentimentScore"],
        "timeSaved": time_saved
    }

    return final_result


if __name__ == "__main__":
    article_link = sys.argv[1]  # The article link passed as the first argument
    result = AI_Tools(article_link)
    print(json.dumps(result))  # Print the result as JSON

