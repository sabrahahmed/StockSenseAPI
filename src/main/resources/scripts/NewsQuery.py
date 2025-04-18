import sys
from groq import Groq
import json

# Initialize the Groq client with your API key
client = Groq(
    api_key="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",
)

def generate_query(stock_symbol):
    # Create a prompt for Groq to generate a search query
    prompt = f"""
    Generate a news search query in the following format based on the stock symbol '{stock_symbol}':
    'stock_symbol stock' 'company_name stock' 'company_name' 'stock_symbol news'
    
    For example:
    For 'MSFT', the query should look like:
    'MSFT stock' 'Microsoft stock' 'Microsoft Corporation' 'MSFT news'
    
    For 'AAPL', the query should look like:
    'AAPL stock' 'Apple stock' 'Apple Inc.' 'AAPL news'
    
    The goal is to generate a concise query with variations of the stock symbol, company name, and relevant keywords like 'stock' or 'news' to get accurate results. Sometimes, the stock symbol doesn't directly fetch the proper results, so be sure to include the full company name when necessary.
    """

    # Send the prompt to Groq to get a completion response
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

    return response


if __name__ == "__main__":
    # Get the stock symbol passed as the first argument
    stock_symbol = sys.argv[1]  # Example: 'MSFT'

    # Generate the query using Groq
    query = generate_query(stock_symbol)

    # Print the query in JSON format to be used in your Java service
    print(json.dumps({"query": query}))
