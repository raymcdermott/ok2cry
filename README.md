# ok2cry
- You: Wow! I love this. I want to use this to encrypt everything, everywhere, always. 
- Me: You can't. Or at least you shouldn't. And yes, it's ok2cry.

This project is about how to use simple, disposable PKI without the tears. And without the ~~bitcon~~ ~~bitcoin~~ bitcon.

The idea is that cryptography is everywhere, and it's free, so you can use it liberally and disposably
- your programming language and OS have cryptographic tools
- mobile devices have cryptographic tools
- embedded devices, even very small and cheap kits, have cryptographic tools
- web browsers, including ~~Oprah~~ Opera, have a built-in cryptographic tools

# Examples
- Protect data coming from a server to a client
- Protect data coming from a server to a server (peers)
- Protect data coming from a server to a client
- Protect data coming from a client to a client (peers) 

### Hold on there ... we've got TLS for that, and it's fine.
Yes, yes you do. And it's great. And in many cases it's good enough. You can move on.

### Er, OK ... just before I leave, quick follow up. When isn't TLS good enough?
It's not great for every situation. Here are a few examples:
- you want to protect data after it exits TLS but before it hits your service. 
  - you can't risk having that data written into logs
  - you don't want anyone except the person entering the data and your service accessing the data.
- you want to send data to a client and be sure that only that one, exact client can read it.
  - TLS is a general, universal way to protect data for anybody that uses your service. 
  - One way TLS used in browsers, does not help in restricting data visibility per client, by design.
- you don't want enterprise TLS interceptors to read the message.
  - you've got nothing to hide, you just don't want them peeking. You like privacy cos... it's a human right maybe?

### Maybe, I'll stick around. So, is this an identity system?
Ah, no. Definitely not. And that's why you can't use it for everything, everywhere, always. It ok2cry, it really is.
You should know that PKI based identity platforms don't give you a perfect, admin free version of secrecy either. There is always a key to protect, manage and share. Or a set of keys. If it's done well, you will hardly notice it, but it's there. The burden is always there. And it's fine. Usually.

### Hang on though, don't you use keys. I'm getting a headache. Maybe I do want to cry.
It's ok2cry. It really is. I encourage it.
Maybe you missed the **disposable** part of the explanation at the beginning? It was a while ago. It's so good that you came this far.
ok2cry is a way to use cryptography for just one message at a time. Nothing is tied to your identity. Every key lasts for the duration of the message exchange and then its thrown away. Whoosh, and it's gone. Meanwhile, your message was always perfectly encrypted between the two ends of the string. It's not a string, but I'm trying a little poetry here, so work with me.

### Isn't cryptography horribly inefficient? Are you burning the planet too?
Excuse me, no. Absolutely not. This is not bitcon mining. It's very efficient to produce keys. The most commonly used CPUs (mobile, PC) have direct support for cryptography. Even tiny IOT style embedded CPUs / SOCs have cryptographic support eg the [ESP32](https://en.wikipedia.org/wiki/ESP32#Features). You can buy one for less than $10.

