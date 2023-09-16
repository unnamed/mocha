# molang
[![Build Status](https://img.shields.io/github/actions/workflow/status/unnamed/molang/build.yml?branch=main)](https://github.com/unnamed/molang/actions/workflows/build.yml)
[![MIT License](https://img.shields.io/badge/license-MIT-blue)](license.txt)
[![Discord](https://img.shields.io/discord/683899335405994062)](https://discord.gg/xbba2fy)

A lightweight, fast and efficient Molang parser and interpreter for Java 8+. Molang is a
simple **expression-based** language designed for fast and **data-driven** calculation of
values at run-time.

Its focus is to enable low-level systems like animation to support flexible data-driven
behavior, while staying highly performant.

Pretty much everything in this language evaluates to a number; if something doesn't evaluate
to a number, you can use an operator to make it into one. You can basically just think of
Molang as one big math equation.

This library lets programmers easily evaluate Molang expressions and bind objects
or functions so that they can be called from the evaluated expressions.

Check the [documentation](https://unnamed.team/docs/molang) for installation, usage and
some more information for this project