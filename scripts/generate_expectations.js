#!/usr/bin/node

/*
 * Small script to generate expectations.txt file inside the
 * src/test/resources folder, they contain the results of
 * evaluating the expressions inside the 'tests.txt' (using
 * MolangJS) so they can be compared with the results of this
 * library
 */
const readline = require('readline');
const fs = require('fs');
const parser = new (require('./lib/molang'))();

// create read/write streams to generate
// results
const basePath = 'src/test/resources';
const input = fs.createReadStream(`${basePath}/tests.txt`);
const output = fs.createWriteStream(`${basePath}/expectations.txt`);

// read input, parse and write
const reader = readline.createInterface({ input });

// write warning
output.write(
        '#\n' +
        '# AUTO-GENERATED FILE, DO NOT MODIFY\n' +
        '# This is an automatically generated file to compare\n' +
        '# results across different Molang libraries, to update\n' +
        '# this file, run the "generateExpectations" gradle task\n' +
        '# by executing "./gradlew generateExpectations"\n' +
        '#\n'
);

reader.on('line', expression => {
    if (expression.length !== 0 && expression.trim().charAt(0) !== '#') {
        const result = parser.parse(expression);
        output.write(
                '\n' +
                '# ' + expression + ' \n' +
                result.toString() + '\n'
        );
    }
});