JavaPengines
============

Java language client for SWI-Prolog's *pengines* library for distributed Prolog.
JavaPengines is a client library for Torbjörn Lager's *Pengines* distributed computing library for
*SWI-Prolog*

Installation
------------

This library, which is available as a jar from Maven Central with groupID com.simularity and ArtifactID com.simularity.os.javapengine, or as sources at [Github](http://www.github.com/Simularity/Javapengine/).

*JavaPengine* requires *javax.json*

Understanding JavaPengine
-------------------------

JavaPengine is a thin wrapper around [http://pengines.swi-prolog.org/docs/index.html](Pengines), and so best use requires knowledge of the Pengines system, although only minimal understanding of pengines or of Prolog is sufficient to make basic queries

Unlike imperative programs, in Prolog one constructs a "knowledgebase" of rules about a world, and then asks the system to find proofs of propositions, called queries. So there are two parts to a Prolog program - the rules, and the query.

SWI-Prolog is probably the most widely used implementation of Prolog, particularly suitable for large _real world_ projects.

Pengines extends SWI-Prolog to provide a distributed computing environment. Pengines exposes an HTTP based protocol that allows a remote system to submit queries and even alter the knowledgebase.

This is not unlike how web servers supply Javascript to the browser to execute, with the client in role of the server and the pengine server in role of the web browser.

Obviously doing this in an unconstrained way would be a huge security issue. So pengines executes the remote-supplied code in a sandbox.

Each created pengine creates, effectively, it's own Prolog sub-VM. Different pengine slaves see different knowledgebases. The pengine's knowledgebase is a combination of what the server chooses to expose, and what the client supplies. The server supplied part includes the safe parts of the standard Prolog libraries. For a complete explanation of this process, watch [https://www.youtube.com/watch?v=JmOHV5IlPyU](my video from Strange Loop)

This provides lots of benefits. The remote client has a full, Turing complete language available for writing queries. Need some complex natural language scoring function computed to decide whether you need that row of data? Do it on the server.

The remote client can also store data on the server between invocations. Need to hang onto an intermediate stage query? Leave it on the server. Need to do graph traversal looking things? Do it on the server. Have a really complicated query you don't want to transmit for each query? Leave the code for it on the server.

Life Cycle
----------

A slave pengine is created, used for zero or more queries, and then destroyed, either explicitly or by timing out.
A common, but not universal, pattern is to create a pengine, query it once, then destroy it. So JavaPengine supports this by allowing you to just make a query and repeatedly ask for the answers.

Prolog queries may return a single answer, but they can also fail (return no answer) or return many answers. This is fundamental to the SLD resolution mechanism at the core of Prolog. But often we know that queries will return a single answer, and we call this being _deterministic_ or _det_. Those other queries are _nondeterministic_, or _nondet_.

JavaPengine has special support for making deterministic queries, and for making a single query and then destroying the pengine. This support is not only for convenience, but for efficiency.

In it's most basic use, the pengines protocol requires one HTTP request to create the pengine, one to initiate a query and get the first answer, one for each answer thereafter, and one to destroy the pengine.

But the pengines protocol allows the client to send the first query with the create message. This saves an HTTP round trip. The protocol also contains a flag that says 'destroy this pengine at the end of the first query'. This saves another round trip.  For a pengine that is used for a single deterministic query, this reduces the number of HTTP requests from 3 to 1, a factor of 3 reduction in network traffic.

The getQuery and getProof methods use these optimizations internally. Their use when appropriate can reduce network traffic.

API
---

TODO will write this when it's more ready

Templates
---------

TODO


