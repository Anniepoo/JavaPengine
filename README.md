JavaPengines
============

A Java language client for Torbjörn Lager's _Pengines_ distributed computing library for
_[SWI-Prolog](http://swi-prolog.org)_ .

What You need to know to use JavaPengines
-----------------------------------------

ldgkjsdflkdsjfdsds

Installation
------------

This library is available as a jar from Maven Central with groupID com.simularity and ArtifactID com.simularity.os.javapengine, or as sources at [Github](http://www.github.com/Simularity/Javapengine/).

*JavaPengine* requires *javax.json*

The Pengines Architecture
-------------------------

need the whole master-slave architecture section in here

A non-Prolog Programmer's Guide to Prolog Queries
-------------------------------------------------

If you know Prolog you can skip this section. If not, it should give you enough understanding to make most Pengines queries.

you call some predicate. If you put a variable in that position it gets filled in, if a constant it's a where. If you add a neck and a guard it's also a where. You get all the ways Prolog can figure out to fullfill the query (all the proofs).

variables UC atoms lc

prolog atoms, strings, 

you can use any sandbox safe Prolog predicate in the library or any predicate exposed by the server.

you can string them together to make more complex queries

you can store your queries on the server.






wannu learn prolog do blah blah blah.

drop the sql analogy


Consider an SQL query. We (the client) run a (usually small) piece of code (the query) against a set of facts (the contents of the database) in an environment (the DBMS) on a server.

Pengines is similar. We (the client) run a (usually small) piece of code (the query) against a set of facts and rules (the pengine knowledgebase) in an environment (the slave pengine) on a pengines server.

The Prolog knowledgebase is made up of _predicates_, things that are true. Prolog assumes that anything not known to be true is false.

SQL databases contain data - what Prolog calls 'facts'. Bob Smith is an assistant manager who was hired on July 2nd, 2004. A small change makes this a predicate - "It is true that Bob Smith is blah blah blah".

.Prolog facts
----
% some facts about a couple employees
employee('Bob Smith', 'assistant manager', date(7,2,2004)).
employee('Sally Jones', 'welder', date(8,22,2012)).

% facts that establish that these are management positions
management_position('director').
management_position('manager').
management_position('assistant manager').
management_position('CEO').
----

When we query an SQL database we provide some variables to be filled in with values.

----
SELECT name, position FROM employees;
----

we get a row for every combination of name and position.


Now is where things start to diverge. Prolog databases also contain _rules_, predicates that are true only if some other predicates are true.


.Prolog rules
----
% a manager is an employee who holds a position that is a management position
manager(Name) :-
   employee(Name, Position, _),
   management_position(Position).
----

And there's no real distinction between facts and rules to the consumer. Lets suppose besides the CEO we also have a CFO, a CTO, and some other executives. We've already got facts that establish what the executive positions are

.Executives
----
executive_position('CEO').
executive_position('CFO').
executive_position('CTO').
executive_position('COO').
----

We don't have to type all those in to make them management positions. We can make a rule. Lets rewrite our first example.

.Now with the executives
----
% some facts about a couple employees
employee('Bob Smith', 'assistant manager', date(7,2,2004)).
employee('Sally Jones', 'welder', date(8,22,2012)).

% a management position is either one of the explicitly named positions, or an executive position
management_position('director').
management_position('manager').
management_position('assistant manager').
management_position(Position) :- 
    executive_position(Position).
----







Name and Position in the above are _variables_. 




The Pengines Protocol
---------------------

_Pengines_ is a protocol for sharing _knowledge_ between a client application and a _pengines server_. _JavaPengines_ is a Java language library for Pengines.

The query language for Pengines is _Prolog_, and so unsurprisingly, the only current implementation of the Pengines server ships with the _[SWI-Prolog](http://swi-prolog.org)_ environment. SWI-Prolog is probably the most widely used implementation of Prolog, particularly suitable for large _real world_ projects.

Pengines makes this even more explicit.  Creating a Pengine gives the client a sandboxed VM on the server in 



Understanding JavaPengine
-------------------------

JavaPengine is a thin wrapper around [http://pengines.swi-prolog.org/docs/index.html](Pengines), and so best use requires knowledge of the Pengines system, although only minimal understanding of pengines or of Prolog is sufficient to make basic queries.

Unlike imperative programs, in Prolog one constructs a "knowledgebase" of rules about a world, and then asks the system to find proofs of propositions, called queries. So there are two parts to a Prolog program - the rules, and the query.

Pengines extends SWI-Prolog to provide a distributed computing environment. Pengines exposes an HTTP based protocol that allows a remote system to submit queries and even alter the knowledgebase.

This is not unlike how web servers supply JavaScript to the browser to execute, with the client in role of the server and the pengine server in role of the web browser.

Obviously doing this in an unconstrained way would be a security issue. So pengines executes the remote-supplied code in a sandbox.

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


