# ok2cry
You: Wow! I want to use this to encrypt everything, everywhere, always. 
Me: You can't. It's ok2cry.

This project is about how to use simple, disposable PKI without the tears.

The fundamental idea is that crypto is everywhere and that means you can use it liberally and disposably
- your programming language and OS have cryptographic tools
- mobile devices have cryptographic tools
- embedded devices, even very small and cheap kits, have cryptographic chips and tools
- web browsers have a built-in cryptographic JS library

# Examples
- Protect data coming from a server to a client
- Protect data coming from a server to a server (peers)
- Protect data coming from a server to a client
- Protect data coming from a client to a client (peers) 

## Hold on there ... we've got TLS
Yes, yes you do. And that's great. And in many cases it's good enough. You can move on.

## Er, OK ... just before I leave, quick follow up. When isn't TLS good enough.
A few examples
- you want to protect data after it exits TLS but before it hits your service. 
  - you can't risk having that data written into logs
  - you don't want anyone except the person entering the data and your service accessing the data.
- you don't want corporate SSL intercepters to read the message. 
  - you've got nothing to hide, you just don't want them peeking. You like privacy cos... it's a human right maybe?
- you want to send data to a client and be sure that only that one, exact client can read it. 
  - TLS is a general, universal way to protect data for anybody.




