#JavaPengines

A Java language client for Torbjörn Lager's _Pengines_ distributed computing library for
_[SWI-Prolog](http://swi-prolog.org)_ .

JavaPengines is a Java language implementation of the _Pengines_ protocol, a lightweight, powerful query and update protocol for a _Pengines_ server.

Pengines servers dramatically reduce the complexity of performing RPC and sharing _knowledge_ in a mixed technology environment. They can act effectively as 'glue' in a complex system.

Currently there are clients for JavaScript (in browser), SWI-Prolog, and Java. We expect clients for Ruby, Python, and NodeJS to be written in the next few months.

The query language for Pengines is _Prolog_, and so unsurprisingly, the only current implementation of the Pengines server ships with the _[SWI-Prolog](http://swi-prolog.org)_ environment. SWI-Prolog is probably the most widely used implementation of Prolog, particularly suitable for large _real world_ projects.


## What You need to know to use JavaPengines

You do *not* need to be an expert in Prolog. The section below should teach you enough to make basic Pengines queries.

You do need to know Java, obviously.

You would also do well to familiarize yourself with the javax.json library.

##Installation

JavaPengines is available as a jar from Maven Central with groupID com.simularity and ArtifactID com.simularity.os.javapengine, or as sources at [Github](http://www.github.com/Simularity/Javapengine/).

JavaPengines requires _javax.json_, which is available from Maven Central. 


## Understanding JavaPengine

JavaPengine is a thin wrapper around [http://pengines.swi-prolog.org/docs/index.html](Pengines), and so use requires knowledge of the Pengines system, although only minimal understanding of pengines or of Prolog is sufficient to make basic queries.

The Pengine architecture is simple. The client requests the server to create a Pengine _slave_. The client then sends one or more queries to the slave, and then tells the server to destroy the pengine.

For efficiency, a query can be sent along with the create query. The pengine can be told to destroy itself at the end of the first query. So a Pengine can be created, queried, and destroyed in as little as a single HTTP request.

The queries are simply Prolog code. So the entire power of the Prolog language is available to the client.

Obviously the Pengine server must _sandbox_ the query. So some Prolog library predicates (e.g. shell) are unavailable. But, as much as is consistent with security, the standard Prolog libraries are available to the Pengine slave.

Additionally, Pengine servers usually expose some special predicates (Prolog 'functions' are called predicates). So, for example, a Prolog server could expose a predicate that allows a user to set their profile (presumably also passing some authentication).

Because the Pengine can last longer than one query, the client can store information on the server between queries. This can significantly reduce network traffic during complex graph queries.

### The Pengines Architecture

Unlike imperative programs, in Prolog one constructs a "knowledgebase" of rules about a world, and then asks the system to find proofs of propositions, called queries. So there are two parts to a Prolog program - the rules, and the query.

Pengines extends SWI-Prolog to provide a distributed computing environment. Pengines exposes an HTTP based protocol that allows a remote system to submit queries and even alter the knowledgebase.

This is not unlike how web servers supply JavaScript to the browser to execute, with the client in role of the server and the pengine server in role of the web browser.

Each created pengine creates, effectively, it's own Prolog sub-VM. Different pengine slaves see different knowledgebases. 

The pengine's knowledgebase is a combination of what the server chooses to expose, and what the client supplies. The server supplied part includes the safe parts of the standard Prolog libraries. 

For a complete explanation of this process, watch [https://www.youtube.com/watch?v=JmOHV5IlPyU](my video from Strange Loop)

This provides lots of benefits. The remote client has a full, Turing complete language available for writing queries. Need some complex natural language scoring function computed to decide whether you need that row of data? Do it on the server.

The remote client can also store data on the server between invocations. Need to hang onto an intermediate stage query? Leave it on the server. Need to do graph traversal looking things? Do it on the server. Have a really complicated query you don't want to transmit for each query? Leave the code for it on the server.

### Life Cycle

A slave pengine is created, used for zero or more queries, and then destroyed, either explicitly or by timing out.
A common, but not universal, pattern is to create a pengine, query it once, then destroy it. So JavaPengine supports this by allowing you to just make a query and repeatedly ask for the answers.

Making a Pengine for a single query is so common that it is the default for PengineBuilder. To retain the Pengine you must call setDestroy(false) on the PengineBuilder.

Prolog queries may return a single answer, but they can also fail (return no answer) or return many answers. This is fundamental to the SLD resolution mechanism at the core of Prolog.

In it's most basic use, the pengines protocol requires one HTTP request to create the pengine, one to initiate a query and get the first answer, one for each answer thereafter, and one to destroy the pengine.

But the pengines protocol allows the client to send the first query with the create message. This saves an HTTP round trip. The protocol also contains a flag that says 'destroy this pengine at the end of the first query'. This saves another round trip.  For a pengine that is used for a single deterministic query, this reduces the number of HTTP requests from 3 to 1, a factor of 3 reduction in network traffic.

The getQuery and getProof methods use these optimizations internally. Their use when appropriate can reduce network traffic.

API
---

Making a Pengine starts with `com.simularity.os.javapengine.PengineBuilder`. 

Make a  new PengineBuilder object, set some values on it, and then user it to make one or more Pengines.

the `setDestroy(boolean)` method is particularly important to set. By default it's true, and when the first query done on the pengine has returned it's last result, the pengine destroys itself.

Some arguments to create a Pengine change with each Pengine, like ask. Some are usually constant, like the server's name. It can be useful to have a prototype PengineBuilder around and clone it, then change values on the clone before making the Pengine.

Call `newPengine()` to get a new Pengine.

If you supplied an ask to the PengineBuilder via `setAsk(String)`, the Pengine will already be executing a query. You can get the query via `getCurrentQuery()`.

If not, you'll need to start a query with `ask(String)`.

Both of these return a `com.simularity.os.javapengine.Query` object.

if the `hasNext()` method returns true, the query _may_ have more answers. `next()` returns the next Proof, or null if there are none.

The Proof object has a key-value map that maps variables in the query to values. So 

---
    member(X, [a,b,c])
---

would result in `{ "X": "a"}` as the first proof, `{ "X": "b"}` as the second, and `{ "X": "c"}` as the last.

There are convenience methods for extracting common Java types from the JSON structure.

If you want to stop getting solutions before they're exhausted, Query has a `stop()` method.

After you have stopped or exhausted the solutions, you can start another query. Each Pengine can be used for only one query at a time.

When you are done with the Pengine, call destroy() on it. This will happen automatically if you left setDestroy set to true.






