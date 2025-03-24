import asyncio
import websockets
import httpx
import json

from jsmin import jsmin
from websockets.asyncio.server import serve

code_loaded = ""
served_clients = set() # To store different websocket connections

def load_javascript():
    global code_loaded

    with open("code.js", "r", encoding="utf-8") as file:
        code = file.read()
    code_loaded = jsmin(code) # Javascript code minimizer

async def basic_http_get_request(url): # Simple HTTP GET request method
    async with httpx.AsyncClient() as httpClient:
        response = await httpClient.get(url)
        return response.json()

async def new_connection(websocket): # A new instance of this method per new websocket connection
    served_clients.add(websocket)
    print("New client found !!")

    try:
        await websocket.send(code_loaded) # Firstly, send the javascript code

        async for response in websocket: # An asynchronous loop equivalent to while True
            if response.startswith("GET"): # "Request for request" received
                url = response[3:] # Extract the url and make the request
                print(f"A HTTP GET request received to url: {url}")
                http_response = await basic_http_get_request(url)
                print(f"Fetched response from: {url}")
                await websocket.send(json.dumps(http_response)) # Send back to device
            else: # "Request to publish computation result"
                print(f"Response received from device !! ==> {response}")
    except websockets.exceptions.ConnectionClosedOK:
        print("Client disconnected manually !!")
    except websockets.exceptions.ConnectionClosed:
        print("Client disconnected unexpectedly !!")
    finally:
        served_clients.remove(websocket) # No more managing for that websocket connection

async def main():
    print("Websockets... listening connections on port 8080, any interface !!")
    async with serve(new_connection, "0.0.0.0", 8080) as server:
        await server.wait_closed() # Serve until server shutdown

if __name__ == "__main__":
    load_javascript()
    print("Javascript code found and loaded !!")
    asyncio.run(main())
