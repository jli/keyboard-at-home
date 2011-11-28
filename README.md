# keyboard-at-home

Theme music: https://www.youtube.com/watch?v=aDaOgu2CQtI

I type all day. I want to type on a layout that's the product of other
layouts that loved each other very much.

This is a silly little genetic algorithm to find the most fit keyboard
ever. Initialized with a random population, the algorithm introduces
immigrants (more random keyboards), mates keyboards to produce
children, and mutates the whole lot. The best make it to the next
generation.

Fitness is a highly personal function that considers finger strength,
finger stretching, consecutive use of the same finger for different
keys, and "cramped" key sequences (like the first half of "excrement"
on qwerty).

Computing the fitness of all keyboards is distributed. There is a
Clojure console client, which is pretty fast (~100ms for a keyboard),
and a ClojureScript client which is horrendously slow (~5s in Chrome).

After a number of generations has passed (currently 100), the server
starts over with different parameters for radiation (number of
mutations) and immigration (number of randoms introduced in each
generation). For each set of parameters, a graph of the average score
of the top 5 keyboards from each population in the sequence is
displayed.

A demo: http://kbd.circularly.org/

## Todo

* De-uglify the display.
* Refine sparklines (absolute scale for global history).
* Web workers for cljs client.
* Tweak fitness function.
* Refine data used for fitness function.

## Thx

Inspiration from years ago: http://web.archive.org/web/20060721141015/http://www.visi.com/%7Epmk/evolved.html

Inspiration for abstracting over simulation parameters: http://worrydream.com/LadderOfAbstraction/

Brown corpus: https://nltk.googlecode.com/svn/trunk/nltk_data/packages/corpora/brown.zip

Artisinal filler text: http://hipsteripsum.me/

John Resig's sparklines: http://ejohn.org/projects/jspark/

## License

Copyright (C) 2011 WTFPL
