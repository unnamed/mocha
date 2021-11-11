#!/usr/bin/node

/*
 * Small script to create a MoLang REPL to fastly
 * eval a MoLang expression, it's used to easily
 * eval an expression without having to run
 * generate_expectations script
 */
const readline = require('readline');
const parser = new (require('./lib/molang'))();

const reader = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

let continueAsking = true;
function ask() {
    reader.question('\x1b[31m<<< \x1b[0m', expression => {
        console.log(`\x1b[32m>>> \x1b[0m${parser.parse(expression)}`);
        if (continueAsking) {
            ask();
        }
    });
}

console.log(
        '\nWelcome to the MoLang REPL\n',
        'Use \x1b[45;1m Ctrl + C \x1b[0m or \x1b[45;1m Ctrl + D \x1b[0m to exit\n'
);
ask();

reader.on('close', () => {
    continueAsking = false;
    console.log('Bye!');
    process.exit(0);
});