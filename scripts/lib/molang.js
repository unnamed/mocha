/*
 * MIT License
 *
 * Copyright (c) 2019 JannisX11
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
// From JannisX11/MolangJS
// File: dist/molang.cjs.js
// https://github.com/JannisX11/MolangJS/blob/2d0e4fda6d7f7d548747e2dbf7210847e6b6c62d/dist/molang.cjs.js
'use strict';

const radify = n => (((n + 180) % 360) +180) % 360;

var MathUtil = {
    clamp(number, min, max) {
        if (number > max) number = max;
        if (number < min || isNaN(number)) number = min;
        return number;
    },
    random(a, b) {
        return a + Math.random() * (b-a)
    },
    randomInt(a, b) {
        a = Math.ceil(a);
        b = Math.floor(b);
        return a + Math.floor(Math.random() * (b - a + 1));
    },
    dieRoll(num, low, high) {
        num = this.clamp(num, 0, 1e9);
        let sum = 0;
        for (var i = 0; i < num; i++) {
            sum += this.random(low, high);
        }
        return sum;
    },
    dieRollInt(num, low, high) {
        num = this.clamp(num, 0, 1e9);
        let sum = 0;
        for (var i = 0; i < num; i++) {
            sum += this.randomInt(low, high);
        }
        return sum;
    },
    lerp(start, end, lerp) {
        return start + (end - start) * lerp;
    },
    lerpRotate(start, end, lerp) {
        let a = radify(start);
        let b = radify(end);

        if (a > b) [a, b] = [b, a];
        var diff = b-a;
        if (diff > 180) {
            return radify(b + lerp * (360-diff));
        } else {
            return a + lerp * diff;
        }
    },
    inRange(value, min, max) {
        return (value <= max && value >= min) ? 1 : 0;
    },
    all(value, ...to_compare) {
        return (to_compare.findIndex(c => c !== value) === -1) ? 1 : 0;
    },
    any(value, ...to_compare) {
        return to_compare.findIndex(c => c == value) >= 0 ? 1 : 0;
    },
    approxEq(value, ...to_compare) {
        return (to_compare.findIndex(c => Math.abs(value - c) > 0.0000001) === -1) ? 1 : 0;
    },
};

/**
 * Author: JannisX11
 * License: MIT
 */



function Molang() {

    const self = this;

    this.global_variables = {};
    this.cache_enabled = true;
    this.use_radians = false;
    this.variables = {};

    this.variableHandler = null;

    let temp_variables = {};
    let found_unassigned_variable = false;
    let loop_status = 0;

    let cached = {};
    let cache_size = 0;
    function addToCache(input, expression) {
        cached[input] = expression;
        cache_size++;
        if (cache_size > 400) {
            // Free some cache
            let keys = Object.keys(cached);
            for (let i = 0; i < 10; i++) {
                delete cached[keys[i]];
            }
            cache_size -= 10;
        }
    }

    // Tree Types
    function Scope(lines) {
        this.lines = [];
        for (let line of lines) {
            if (!line) continue;
            let result = iterateString(line);
            this.lines.push(result);
            if (result instanceof ReturnStatement) break;
        }
    }
    function Loop(iterations, body) {
        this.iterations = iterateString(iterations);
        this.body = iterateString(body);
    }
    function Comp(operator, a, b, c) {
        this.operator = operator;
        this.a = iterateString(a);
        if (b !== undefined) this.b = iterateString(b);
        if (c !== undefined) this.c = iterateString(c);
    }
    function QueryFunction(query, args) {
        this.query = query;
        this.args = args.map(string => iterateString(string));
    }
    function Allocation(name, value) {
        this.value = iterateString(value);
        this.name = toVariableName(name);
    }
    function ReturnStatement(value) {
        this.value = iterateString(value);
    }
    function BreakStatement() {}
    function ContinueStatement() {}

    let angleFactor = () => this.use_radians ? 1 : (Math.PI/180);

    let string_num_regex = /^-?\d+(\.\d+f?)?$/;
    function isStringNumber(string) {
        return string_num_regex.test(string);
    }

    function toVariableName(input) {
        if (input[1] === POINT) {
            let char = input[0];
            switch (char) {
                case 'q': return 'query' + input.substring(1);
                case 'v': return 'variable' + input.substring(1);
                case 't': return 'temp' + input.substring(1);
                case 'c': return 'context' + input.substring(1);
                default:  return input;
            }
        } else {
            return input;
        }
    }

    const logic_operator_regex = /[&|<>=]/;
    const allocation_regex = /^(temp|variable|t|v)\.\w+=/;
    const TRUE = 'true';
    const FALSE = 'false';
    const RETURN = 'return';
    const BREAK = 'break';
    const CONTINUE = 'continue';
    const POINT = '.';
    function iterateString(s) {
        //Iterates through string, returns float, string or comp;
        if (!s) return 0;

        if (s.endsWith(';')) s = s.substring(0, s.length-1);
        while (canTrimBrackets(s)) {
            s = s.substr(1, s.length-2);
        }

        if (isStringNumber(s)) return parseFloat(s);

        let lines = splitStringMultiple(s, ';');
        if (lines) {
            return new Scope(lines);
        }

        //Return Statement
        if (s.startsWith(RETURN)) {
            return new ReturnStatement(s.substr(6))
        }

        // Bool
        switch (s) {
            case TRUE: return 1;
            case FALSE: return 0;
            case BREAK: return new BreakStatement();
            case CONTINUE: return new ContinueStatement();
        }

        let has_equal_sign = s.indexOf('=') !== -1;

        //allocation
        let match = has_equal_sign && s.length > 4 && s.match(allocation_regex);
        if (match && s[match.index + match[0].length] !== '=') {
            let name = match[0].substring(0, match[0].length-1);
            let value = s.substr(match.index + match[0].length);
            return new Allocation(name, value)
        }

        let has_question_mark = s.indexOf('?') !== -1;

        // Null Coalescing
        let comp = has_question_mark && testOp(s, '??', 19);
        if (comp) return comp;

        //ternary
        let split = has_question_mark && splitString(s, '?');
        if (split) {
            let ab = splitString(split[1], ':');
            if (ab && ab.length) {
                return new Comp(10, split[0], ab[0], ab[1]);
            } else {
                return new Comp(10, split[0], split[1], 0);
            }
        }

        //2 part operators
        let has_logic_operators = logic_operator_regex.test(s);
        comp = (
                has_logic_operators && (
                        testOp(s, '&&', 11) ||
                        testOp(s, '||', 12) ||
                        (has_equal_sign && testOp(s, '==', 17)) ||
                        (has_equal_sign && testOp(s, '!=', 18)) ||
                        (has_equal_sign && testOp(s, '<=', 14)) ||
                        testOp(s, '<', 13) ||
                        (has_equal_sign && testOp(s, '>=', 16)) ||
                        testOp(s, '>', 15)
                ) ||

                testOp(s, '+', 1, true) ||
                testMinus(s, '-', 2) ||
                testOp(s, '*', 3) ||
                testOp(s, '/', 4, true) ||
                testNegator(s)
        );
        if (comp instanceof Comp) return comp;

        if (s.startsWith('math.')) {
            if (s === 'math.pi') {
                return Math.PI
            }
            let begin = s.indexOf('(');
            let operator = s.substr(5, begin-5);
            let inner = s.substr(begin+1, s.length-begin-2);
            let params = splitStringMultiple(inner, ',');
            if (!params) params = [inner];

            switch (operator) {
                case 'abs': 			return new Comp(100, params[0]);
                case 'sin': 			return new Comp(101, params[0]);
                case 'cos': 			return new Comp(102, params[0]);
                case 'exp': 			return new Comp(103, params[0]);
                case 'ln': 				return new Comp(104, params[0]);
                case 'pow': 			return new Comp(105, params[0], params[1]);
                case 'sqrt': 			return new Comp(106, params[0]);
                case 'random': 			return new Comp(107, params[0], params[1]);
                case 'ceil': 			return new Comp(108, params[0]);
                case 'round': 			return new Comp(109, params[0]);
                case 'trunc': 			return new Comp(110, params[0]);
                case 'floor': 			return new Comp(111, params[0]);
                case 'mod': 			return new Comp(112, params[0], params[1]);
                case 'min': 			return new Comp(113, params[0], params[1]);
                case 'max': 			return new Comp(114, params[0], params[1]);
                case 'clamp': 			return new Comp(115, params[0], params[1], params[2]);
                case 'lerp': 			return new Comp(116, params[0], params[1], params[2]);
                case 'lerprotate': 		return new Comp(117, params[0], params[1], params[2]);
                case 'asin': 			return new Comp(118, params[0]);
                case 'acos': 			return new Comp(119, params[0]);
                case 'atan': 			return new Comp(120, params[0]);
                case 'atan2': 			return new Comp(121, params[0], params[1]);
                case 'die_roll': 		return new Comp(122, params[0], params[1], params[2]);
                case 'die_roll_integer':return new Comp(123, params[0], params[1], params[2]);
                case 'hermite_blend': 	return new Comp(124, params[0]);
                case 'random_integer': 	return new Comp(125, params[0], params[1]);
            }
        }
        if (s.startsWith('loop(')) {
            let inner = s.substring(5, s.length-1);
            let params = splitStringMultiple(inner, ',');
            if (params) {
                return new Loop(...params)
            }
        }

        split = s.match(/[a-z0-9._]{2,}/g);
        if (split && split.length === 1 && split[0].length >= s.length-2) {
            return toVariableName(s);
        } else if (s.includes('(') && s[s.length-1] == ')') {
            let begin = s.search(/\(/);
            let query_name = toVariableName(s.substr(0, begin));
            let inner = s.substr(begin+1, s.length-begin-2);
            let params = splitStringMultiple(inner, ',');
            if (!params) params = [inner];

            return new QueryFunction(query_name, params);
        }
        return 0;
    }
    function canTrimBrackets(s) {
        let regular_brackets = s.startsWith(BracketOpen) && s.endsWith(BracketClose);
        if (
                regular_brackets || (s.startsWith(CurlyBracketOpen) && s.endsWith(CurlyBracketClose))
        ) {
            if (s.indexOf(regular_brackets ? BracketClose : CurlyBracketClose) === s.length-1) return true;
            let level = 0;
            for (let i = 0; i < s.length-1; i++) {
                switch (s[i]) {
                    case BracketOpen:  case CurlyBracketOpen:  level++; break;
                    case BracketClose: case CurlyBracketClose: level--; break;
                }
                if (level == 0) return false;
            }
            return true;
        } else {
            return false;
        }
    }
    function testOp(s, char, operator, inverse) {

        let split = inverse ? splitStringReverse(s, char) : splitString(s, char);
        if (split) {
            return new Comp(operator, split[0], split[1])
        }
    }
    function testMinus(s, char, operator) {

        let split = splitStringReverse(s, char);
        if (split) {
            if (split[0].length === 0) {
                return new Comp(operator, 0, split[1])
            } else {
                return new Comp(operator, split[0], split[1])
            }
        }
    }
    function testNegator(s) {
        if (s.startsWith('!') && s.length > 1) {
            return new Comp(5, s.substr(1), 0)
        }
    }
    const BracketOpen = '(';
    const BracketClose = ')';
    const CurlyBracketOpen = '{';
    const CurlyBracketClose = '}';
    const Minus = '-';
    function splitString(s, char) {
        if (s.indexOf(char) === -1) return;
        let level = 0;
        for (let i = 0; i < s.length; i++) {
            switch (s[i]) {
                case BracketOpen: level++; break;
                case BracketClose: level--; break;
                default:
                    if (level === 0 && char[0] === s[i] && (char.length === 1 || char === s.substr(i, char.length))) {
                        return [
                            s.substr(0, i),
                            s.substr(i+char.length)
                        ];
                    }
                    break;
            }
        }
    }
    function splitStringMultiple(s, char) {
        if (s.indexOf(char) === -1) return;
        let level = 0;
        let pieces;
        let last_split = 0;
        loop:
                for (let i = 0; i < s.length; i++) {
                    switch (s[i]) {
                        case BracketOpen: case CurlyBracketOpen: level++; break;
                        case BracketClose: case CurlyBracketClose: level--; break;
                        default:
                            if (level === 0 && char[0] === s[i] && (char.length === 1 || char === s.substr(i, char.length))) {
                                let piece = s.substring(last_split, i);
                                if (!pieces) pieces = [];
                                pieces.push(piece);
                                last_split = i + char.length;
                                if (s.substring(last_split).indexOf(char) === -1) break loop;
                            }
                    }
                }
        if (pieces && pieces.length) {
            pieces.push(s.substring(last_split));
            return pieces;
        }
    }
    function splitStringReverse(s, char) {
        if (s.indexOf(char) === -1) return;
        let i = s.length-1;
        let level = 0;
        while (i >= 0) {
            switch (s[i]) {
                case BracketOpen: case CurlyBracketOpen: level++; break;
                case BracketClose: case CurlyBracketClose: level--; break;
                default:
                    if (level === 0 && char[0] === s[i] &&
                            (char.length === 1 || char === s.substr(i, char.length)) &&
                            (char !== Minus || '+*/<>=|&?:'.includes(s[i-1]) === false)
                    ) {
                        return [
                            s.substr(0, i),
                            s.substr(i+char.length)
                        ];
                    }
                    break;
            }
            i--;
        }
    }
    function compareValues(a, b) {
        if (!(typeof a === 'string' && a[0] == `'`)) a = iterateExp(a, true);
        if (!(typeof b === 'string' && b[0] == `'`)) b = iterateExp(b, true);
        return a === b;
    }
    function iterateExp(T, allow_strings) {
        if (typeof T === 'number') {
            return T;
        } else if (typeof T === 'string') {
            let val = temp_variables[T];
            if (val === undefined && typeof self.variableHandler === 'function') {
                val = self.variableHandler(T, temp_variables);
            }
            if (typeof val === 'number') {
                return val;
            } else if (typeof val === 'string' && !allow_strings) {
                return self.parse(val, temp_variables) || 0;
            } else if (val === undefined) {
                found_unassigned_variable = true;
            } else if (typeof val === 'function') {
                return val() || 0;
            }
            return val || 0;
        } else if (T === undefined) {
            return 0;
        }
        switch (T.constructor) {
            case Comp:

                switch (T.operator) {
                        //Basic
                    case 1:		return iterateExp(T.a) + iterateExp(T.b);
                    case 2:		return iterateExp(T.a) - iterateExp(T.b);
                    case 3:		return iterateExp(T.a) * iterateExp(T.b);
                    case 4:		return iterateExp(T.a) / iterateExp(T.b);
                    case 5:		return iterateExp(T.a) == 0 ? 1 : 0;

                        //Logical
                    case 10:	return iterateExp(T.a) ?  iterateExp(T.b) : iterateExp(T.c);
                    case 11:	return iterateExp(T.a) && iterateExp(T.b) ? 1 : 0;
                    case 12:	return iterateExp(T.a) || iterateExp(T.b) ? 1 : 0;
                    case 13:	return iterateExp(T.a) <  iterateExp(T.b) ? 1 : 0;
                    case 14:	return iterateExp(T.a) <= iterateExp(T.b) ? 1 : 0;
                    case 15:	return iterateExp(T.a) >  iterateExp(T.b) ? 1 : 0;
                    case 16:	return iterateExp(T.a) >= iterateExp(T.b) ? 1 : 0;
                    case 17:	return compareValues(T.a, T.b) ? 1 : 0;
                    case 18:	return compareValues(T.a, T.b) ? 0 : 1;
                    case 19:	found_unassigned_variable = false;
                        let variable = iterateExp(T.a);
                        return found_unassigned_variable ? iterateExp(T.b) : variable;

                        //Math
                    case 100:	return Math.abs(iterateExp(T.a));
                    case 101:	return Math.sin(iterateExp(T.a) * angleFactor());
                    case 102:	return Math.cos(iterateExp(T.a) * angleFactor());
                    case 103:	return Math.exp(iterateExp(T.a));
                    case 104:	return Math.log(iterateExp(T.a));
                    case 105:	return Math.pow(iterateExp(T.a), iterateExp(T.b));
                    case 106:	return Math.sqrt(iterateExp(T.a));
                    case 107:	return MathUtil.random(iterateExp(T.a), iterateExp(T.b));
                    case 108:	return Math.ceil(iterateExp(T.a));
                    case 109:	return Math.round(iterateExp(T.a));
                    case 110:	return Math.trunc(iterateExp(T.a));
                    case 111:	return Math.floor(iterateExp(T.a));
                    case 112:	return iterateExp(T.a) % iterateExp(T.b);
                    case 113:	return Math.min(iterateExp(T.a), iterateExp(T.b));
                    case 114:	return Math.max(iterateExp(T.a), iterateExp(T.b));
                    case 115:	return MathUtil.clamp(iterateExp(T.a), iterateExp(T.b), iterateExp(T.c));
                        //	Lerp
                    case 116:	return MathUtil.lerp(iterateExp(T.a), iterateExp(T.b), iterateExp(T.c));
                    case 117:	return MathUtil.lerpRotate(iterateExp(T.a), iterateExp(T.b), iterateExp(T.c));
                        // Inverse Trigonometry
                    case 118:	return Math.asin(iterateExp(T.a)) / angleFactor();
                    case 119:	return Math.acos(iterateExp(T.a)) / angleFactor();
                    case 120:	return Math.atan(iterateExp(T.a)) / angleFactor();
                    case 121:	return Math.atan2(iterateExp(T.a), iterateExp(T.b)) / angleFactor();
                        // Misc
                    case 122:	return MathUtil.dieRoll(iterateExp(T.a), iterateExp(T.b), iterateExp(T.c));
                    case 123:	return MathUtil.dieRollInt(iterateExp(T.a), iterateExp(T.b), iterateExp(T.c));
                    case 124:
                        let t = iterateExp(T.a);
                        return 3*Math.pow(t, 2) - 2*Math.pow(t, 3);
                    case 125:	return MathUtil.randomInt(iterateExp(T.a), iterateExp(T.b));
                }
                break;

            case ReturnStatement:
                loop_status = 1;
                return iterateExp(T.value);

            case Allocation:
                temp_variables[T.name] = self.variables[T.name] = iterateExp(T.value);
                return 0;

            case QueryFunction:

                let args = T.args.map(arg => iterateExp(arg));
                switch (T.query) {
                    case 'query.in_range': 	return MathUtil.inRange(...args);
                    case 'query.all': 		return MathUtil.all(...args);
                    case 'query.any': 		return MathUtil.any(...args);
                    case 'query.approx_eq': return MathUtil.approxEq(...args);
                }
                if (typeof temp_variables[T.query] == 'function') {
                    return temp_variables[T.query](...args) || 0;
                }
                if (typeof self.variableHandler === 'function') {
                    return self.variableHandler(T.query, temp_variables, args) || 0;
                }
                return 0;

            case Scope:
                loop_status = 0;
                let return_value = 0;
                for (let line of T.lines) {
                    return_value = iterateExp(line);
                    if (loop_status > 0) {
                        break;
                    }
                }
                return return_value;

            case Loop:
                let return_value2 = 0;
                let iterations = MathUtil.clamp(iterateExp(T.iterations), 0, 1024);
                for (let i = 0; i < iterations; i++) {
                    let result = iterateExp(T.body);
                    if (loop_status === 2) {
                        loop_status = 0;
                        break;
                    }
                    if (loop_status === 3) {
                        loop_status = 0;
                        continue;
                    }
                    return_value2 = result;
                    if (loop_status === 1) break;
                }
                return return_value2;

            case BreakStatement:
                loop_status = 2;
                return 0;

            case ContinueStatement:
                loop_status = 3;
                return 0;
        }
        return 0;
    }

    function calculate(expression, variables) {
        for (let key in self.global_variables) {
            temp_variables[key] = self.global_variables[key];
        }
        for (let key in self.variables) {
            temp_variables[key] = self.variables[key];
        }
        if (variables) {
            for (let key in variables) {
                temp_variables[key] = variables[key];
            }
        }
        let end_result = iterateExp(expression);
        temp_variables = {};
        loop_status = 0;
        return end_result;
    }

    this.parse = (input, variables) => {
        if (typeof input === 'number') {
            return input || 0;
        }
        if (typeof input !== 'string' || input.length === 0) return 0;
        if (input.length < 9 && isStringNumber(input)) {
            return parseFloat(input);
        }

        let expression = this.cache_enabled && cached[input];
        if (!expression) {
            expression = iterateString(input.toLowerCase().replace(/\s/g, ''));
            if (this.cache_enabled) {
                addToCache(input, expression);
            }
        }
        return calculate(expression, variables) || 0;
    };
    this.resetVariables = () => {
        self.variables = {};
    };
}

module.exports = Molang;