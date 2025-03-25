# dlc-project-pocs
POCs for distributed light computing project

## Description
The dlc project stands for "distributed light computing" and its main objective is to design and to implement a framework for highly-customizable grid computing applications development. The term "light" comes up related to leveraging any avaliable computational power rather than pointing to dedicated machines, so code solutions produced by the framework must target a heterogeneous set of hardware platforms including:

1. Linux devices
1. Windows devices
1. MacOS devices
1. Android devices
1. iOS devices
1. Playstation
1. Xbox

Javascript was our obvious candidate language to start thinking about such objective.

## What does this repo include ?
Three different POCs with progressive complexity to demonstrate basic solutions that might model the project architecture.

### Poc1
Basic app written for Android using Kotlin language. Demonstrates the javascript code injection on a WebView component and how to get the code running on the device.

### Poc2
A step further, adding a Websocket server written in python that sends the javascript code to the app:

1. App request for connection
1. Server accepts and sends the javascript code to the app
1. App gets the javascript code and runs it in a WebView component
1. App computes the result and send it back to the server
1. Server receives the result

### Poc3
A more advanced architecture, in which the javascript code requires to obtain data from a REST API, the app is able to stop the code execution and delegates the request to the python server:

1. App request for connection
1. Server accepts and sends the javascript code to the app
1. App gets the javascript code and runs it in a WebView component
1. Javascript code requires to get data from a REST API, but obviously is not authorized to do it from the WebView ! The execution is paused
1. App detects the request, takes it by itself and elevates it to the python server
1. Python server recognizes that the message received is a request and acts like a proxy, sending the request to the demanded URL
1. Python server receives the response from the endpoint
1. Python server sends the response to the app
1. App receives the response and injects it again to WebView
1. Javascript code running inside WebView detects that the data is now available and continues its execution
1. Eventually the execution finishes, and the computed result is obtained
1. Due to the javascript execution is now asynchronous, App keeps polling the WebView component until getting the result
1. App sends the result back to the server
1. Server receives the result

