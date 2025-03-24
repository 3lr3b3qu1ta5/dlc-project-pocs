import asyncio
import websockets

from jsmin import jsmin
from websockets.asyncio.server import serve

code_loaded = ""
served_clients = set() # To store different websocket connections

def load_javascript():
    global code_loaded

    with open("code.js", "r", encoding="utf-8") as file:
        code = file.read()
    code_loaded = jsmin(code) # Javascript code minimizer

async def new_connection(websocket): # A new instance of this method per new websocket connection
    served_clients.add(websocket)
    print("New client found !!")

    try:
        await websocket.send(code_loaded) # Firstly, send the javascript code

        async for response in websocket: # An asynchronous loop equivalent to while True
            print(f"Response received !! ==> {response}")
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
