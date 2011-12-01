#!/bin/sh

cljs-watch src/keyboard_at_home/ '{:output-to "resources/public/worker.js" :optimizations :advanced}'
