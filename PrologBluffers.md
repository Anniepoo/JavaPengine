
## A non-Prolog Programmer's Guide to Prolog Queries

TODO is this really a good idea?

If you know Prolog you can skip this section. If you know Prolog and are pissed because this section misses the whole point of the language, yup, it does, I know that, it delivers what Pengine users need and no more.

If not, don't worry. Being a Prolog programmer isn't necessary to write JavaPengine client code or construct basic JavaPengine queries. This section should give you enough understanding to make most Pengines queries.

### Prolog code is predicates

All Prolog code is 'predicates', which are a bit like functions, but have important differences.

The set of all predicates that can be seen by a pengine is called it's knowledgebase. The Pengine server has a different knowledgebase, and some parts overlap, but the pengine has it's own knowledgebase.

Queries are just a predicate call, like this 

---
    employee(12345, 'Bob Smith', manager)
---

Prolog runs this and says whether it  can prove this is true. That is, 
"Can you prove that there's an employee with number 12345 named Bob Smith who'se a manager?"

This is usually not that useful, of course. 

Far more useful is 

----
    employee(EmpNumber, 'Bob Smith', manager)
----

This asks "can you prove that there's an employee with number EmpNumber named Bob Smith who'se a manager, and if so, what would EmpNumber
have to be to make it true?"

And we get back EmpNumber has to be 12345.

This means we can do any SELECT/WHERE type clause.

### Variables

EmpNumber is a variable. If you put a variable in a position it gets filled in with a value that makes the whole thing true.
Variables must start with an upper case letter. By convention, they're CamelCase. 

----
    employee(12345, Name, Position)
----

This query returns the Name and Position of employee 12345.

### Constants

manager is a constant, just like 12345. It's an _atom_. Atoms must start with a lowercase letter. By convention atoms_are_lowercase_with_underscores.

Single quoted strings are also atoms. foo and 'foo' are identical in Prolog. This is useful because atoms are also used as strings.

Besides numbers and atoms, double quoted strings are a type (they're strings in SWI-Prolog, and lists of integer ASCII codes in traditional Prologs).

There are three compound types. Lists like [1,2,3,4] and compound terms like foo(3). This latter is _not_ a function call. It's more like a record. SWI-Prolog has also added _dicts_, which are key-value map structures with atom keys, like +pt{x:4.5, y:6.9}+.


----
    employee(12000 + 345, Name, Position)
----

This is NOT the same as the previous query. Arguments aren't evaluated in Prolog! The employee predicate will see '+'(12000,345) as it's first argument. The + operator is just syntactic sugar for '+'(12000,345), a compound term .

So, this process of matching up possible solutions is one big difference between Prolog predicates and most language's functions.

### Multiple Results

The second is that Prolog will give you not just one answer. You get all the ways Prolog can figure out to fullfill the query (all the proofs).

If we query 

----
    employee(Number, Name, manager)
----

and we have several managers, Prolog will give us all the different combinations that satisfy Number and Name. JavaPengines Query class acts like an iterator - successive items give successive proofs.

### Stringing together predicates

You can put several predicates in a query and string them together with commas, surrounding the whole thing with parens. This restricts the solutions to only those that satisfy all the parts.

----
    (employee(Number, Name, Position), member(Position, [manager, assistant_manager])
----

This is true for all combinations of Number and Name that describe managers or assistant managers.

member is a library predicate in SWI-Prolog, and is available in the sandbox. It's true when it's first argument is a member of the list made up of it's second argument.

By stringing together predicates with commas we can make quite complex queries. 

Suppose we have another predicate salary.

----
    salary(12345, 122000).
    salary(34625, 56000).
----

We can find out Bob Smith's salary 

----
    (employee(Number, 'Bob Smith', _), salary(Number, Salary))
----

Which is true only when Number is Bob's number, and salary is his Salary.

This example also introduces the underscore. We really weren't interested in Bob's position, so we used a special variable, the underscore, which means 'I don't care what you put in here'.

### Moving Forward

You can do a lot more with Prolog. We wanted to give users enough to make simple queries.

Prolog is a full fledged computer language. There are large commercial applications written entirely in Prolog, everything from CAD systems to stock trading systems.

You can do a lot more with Pengines. You can place additional code in your pengine and then query your own code. You have the same power at your disposal as a programmer working locally in Prolog. Pengines makes a good 'glue' to hold large systems together.

If you want to learn Prolog, a good starting place is the book "Programming in Prolog" by William Clocksin and Chris Mellish. An online resource is [http://lpn.swi-prolog.org/](Learn Prolog Now), an interactive Prolog course (that uses Pengines, by the way). The ##prolog channel on freenode.net IRC is often helpful.

the website for [http://swi-prolog.org](SWI-Prolog) is the authority on SWI-Prolog. The search tool is your gateway to discovering the extensive SWI-Prolog libraries. 

If you want to experiment with Prolog, one particularly easy way is via the [http://swish.swi-prolog.org](SWISH online tool). This IDE in a web page environment is a great way to learn. It's implemented using pengines, so you're using the same environment you'll be using to query Pengines.
