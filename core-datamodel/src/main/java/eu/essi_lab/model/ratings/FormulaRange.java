package eu.essi_lab.model.ratings;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A single piece of a piece-wise rating-curve {@link Formula}: a water-level validity range together with the
 * discharge sub-formula {@code Q = f(h)} that applies within it.
 * <p>
 * The sub-formula is a mathematical expression in the variable {@code h} (water level) supporting {@code + - * / ^},
 * parentheses, unary signs, scientific notation and the (case insensitive) spreadsheet functions {@code POWER(x,y)}
 * (or {@code POW}), {@code EXP(x)}, {@code LN(x)}, {@code LOG10(x)}, {@code LOG(x[,base])}, {@code SQRT(x)} and
 * {@code ABS(x)}. Decimal commas are accepted when they cannot be confused with function argument separators.
 * Discharge is obtained through {@link #evaluate(double)}.
 *
 * @author boldrini
 */
public class FormulaRange {

    private static final Pattern FUNCTION_CALL = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*\\s*\\(");

    private static final Pattern BETWEEN = Pattern
	    .compile("^(-?\\d*\\.?\\d+)<=?h<=?(-?\\d*\\.?\\d+)$");
    private static final Pattern UPPER = Pattern.compile("^h<=?(-?\\d*\\.?\\d+)$");
    private static final Pattern LOWER = Pattern.compile("^h>=?(-?\\d*\\.?\\d+)$");
    private static final Pattern AFTER_GT = Pattern.compile(">=?(-?\\d*\\.?\\d+)");
    private static final Pattern AFTER_LT = Pattern.compile("<=?(-?\\d*\\.?\\d+)");

    private final double lowerBound;
    private final double upperBound;
    private final String rangeExpression;
    private final String formulaExpression;

    public FormulaRange(double lowerBound, double upperBound, String rangeExpression, String formulaExpression) {
	this.lowerBound = lowerBound;
	this.upperBound = upperBound;
	this.rangeExpression = rangeExpression;
	this.formulaExpression = formulaExpression;
    }

    /**
     * Parses a range expression (e.g. {@code h<0.34}, {@code h>0.34}, {@code 1.302<h<3.54}) and a discharge formula
     * (e.g. {@code Q=13.197*(h-(-0.053))^1.657}) into a {@link FormulaRange}. Decimal commas are accepted.
     *
     * @return the parsed range, or {@code null} if the inputs are not a recognizable range + formula
     */
    public static FormulaRange parse(String rangeExpression, String formulaExpression) {

	if (rangeExpression == null || formulaExpression == null) {
	    return null;
	}
	String formula = normalizeFormula(formulaExpression);
	if (formula.isEmpty()) {
	    return null;
	}
	String range = rangeExpression.replace(',', '.').toLowerCase().replaceAll("\\s+", "");
	if (range.isEmpty()) {
	    return null;
	}

	Double lower = null;
	Double upper = null;

	Matcher between = BETWEEN.matcher(range);
	if (between.matches()) {
	    lower = Double.parseDouble(between.group(1));
	    upper = Double.parseDouble(between.group(2));
	} else {
	    boolean hasGt = range.indexOf('>') >= 0;
	    boolean hasLt = range.indexOf('<') >= 0;
	    if (hasGt && hasLt) {
		// e.g. "h>0.498e<1.62"
		Matcher gt = AFTER_GT.matcher(range);
		Matcher lt = AFTER_LT.matcher(range);
		if (gt.find() && lt.find()) {
		    lower = Double.parseDouble(gt.group(1));
		    upper = Double.parseDouble(lt.group(1));
		}
	    } else {
		Matcher up = UPPER.matcher(range);
		Matcher lo = LOWER.matcher(range);
		if (up.matches()) {
		    lower = Double.NEGATIVE_INFINITY;
		    upper = Double.parseDouble(up.group(1));
		} else if (lo.matches()) {
		    lower = Double.parseDouble(lo.group(1));
		    upper = Double.POSITIVE_INFINITY;
		}
	    }
	}

	if (lower == null || upper == null) {
	    return null;
	}
	return new FormulaRange(lower, upper, rangeExpression.trim(), formula);
    }

    private static String normalizeFormula(String formula) {

	String f = formula.trim();

	// leading Excel formula marker and string literal quotes, e.g. ="Q=3*POWER(h,2)"
	f = f.replaceFirst("^=", "").trim();
	if (f.length() > 1 && f.startsWith("\"") && f.endsWith("\"")) {
	    f = f.substring(1, f.length() - 1).trim();
	}

	if (f.indexOf(';') >= 0) {
	    // Excel with Italian locale: ';' separates function arguments, ',' is the decimal separator
	    f = f.replace(',', '.').replace(';', ',');
	} else if (!FUNCTION_CALL.matcher(f).find()) {
	    // no function calls: ',' can only be a decimal separator
	    f = f.replace(',', '.');
	}
	// otherwise (e.g. POWER(h+9.52,1.57)) ',' separates function arguments and '.' is the decimal separator

	f = f.replaceFirst("^[Qq]\\s*=\\s*", "").trim();
	return f;
    }

    /**
     * @return {@code true} if {@code level} falls within this range (bounds inclusive)
     */
    public boolean contains(double level) {

	return level >= lowerBound && level <= upperBound;
    }

    /**
     * Evaluates the discharge for the given water level using the sub-formula.
     *
     * @return discharge, or {@link Double#NaN} if the formula cannot be evaluated
     */
    public double evaluate(double level) {

	try {
	    return new ExpressionEvaluator(formulaExpression).evaluate(level);
	} catch (RuntimeException e) {
	    return Double.NaN;
	}
    }

    public double getLowerBound() {
	return lowerBound;
    }

    public double getUpperBound() {
	return upperBound;
    }

    public String getRangeExpression() {
	return rangeExpression;
    }

    public String getFormulaExpression() {
	return formulaExpression;
    }

    @Override
    public String toString() {

	String lo = lowerBound == Double.NEGATIVE_INFINITY ? "-inf" : String.valueOf(lowerBound);
	String hi = upperBound == Double.POSITIVE_INFINITY ? "+inf" : String.valueOf(upperBound);
	return "[" + lo + ", " + hi + "]: Q=" + formulaExpression;
    }

    /**
     * Minimal recursive-descent evaluator for arithmetic expressions in the variable {@code h}.
     * Supports {@code + - * / ^}, parentheses and unary {@code + -}. The power operator is right-associative.
     */
    private static final class ExpressionEvaluator {

	private final String expression;
	private double h;
	private int pos = -1;
	private int ch;

	ExpressionEvaluator(String expression) {
	    this.expression = expression;
	}

	double evaluate(double level) {

	    this.h = level;
	    this.pos = -1;
	    nextChar();
	    double value = parseExpression();
	    if (pos < expression.length()) {
		throw new IllegalArgumentException("Unexpected character: " + (char) ch);
	    }
	    return value;
	}

	private void nextChar() {
	    ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
	}

	private boolean eat(int charToEat) {
	    while (ch == ' ') {
		nextChar();
	    }
	    if (ch == charToEat) {
		nextChar();
		return true;
	    }
	    return false;
	}

	private double parseExpression() {
	    double x = parseTerm();
	    for (;;) {
		if (eat('+')) {
		    x += parseTerm();
		} else if (eat('-')) {
		    x -= parseTerm();
		} else {
		    return x;
		}
	    }
	}

	private double parseTerm() {
	    double x = parseFactor();
	    for (;;) {
		if (eat('*')) {
		    x *= parseFactor();
		} else if (eat('/')) {
		    x /= parseFactor();
		} else {
		    return x;
		}
	    }
	}

	private double parseFactor() {

	    if (eat('+')) {
		return parseFactor();
	    }
	    if (eat('-')) {
		return -parseFactor();
	    }

	    double x;
	    int startPos = pos;
	    if (eat('(')) {
		x = parseExpression();
		if (!eat(')')) {
		    throw new IllegalArgumentException("Missing closing parenthesis");
		}
	    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
		while ((ch >= '0' && ch <= '9') || ch == '.') {
		    nextChar();
		}
		// scientific notation, e.g. 1.5E-3
		if ((ch == 'e' || ch == 'E') && isExponentAhead()) {
		    nextChar();
		    if (ch == '+' || ch == '-') {
			nextChar();
		    }
		    while (ch >= '0' && ch <= '9') {
			nextChar();
		    }
		}
		x = Double.parseDouble(expression.substring(startPos, pos));
	    } else if (Character.isLetter(ch) || ch == '_') {
		while (Character.isLetterOrDigit(ch) || ch == '_') {
		    nextChar();
		}
		String name = expression.substring(startPos, pos);
		if (eat('(')) {
		    x = callFunction(name, parseArguments());
		} else if (name.equalsIgnoreCase("h")) {
		    x = h;
		} else {
		    throw new IllegalArgumentException("Unknown variable: " + name);
		}
	    } else {
		throw new IllegalArgumentException("Unexpected character: " + (char) ch);
	    }

	    if (eat('^')) {
		x = Math.pow(x, parseFactor());
	    }
	    return x;
	}

	/**
	 * @return {@code true} if the current 'e'/'E' starts a valid exponent (digits, optionally signed)
	 */
	private boolean isExponentAhead() {
	    int next = pos + 1;
	    if (next < expression.length() && (expression.charAt(next) == '+' || expression.charAt(next) == '-')) {
		next++;
	    }
	    return next < expression.length() && Character.isDigit(expression.charAt(next));
	}

	/**
	 * Parses a comma separated list of arguments; the opening parenthesis is already consumed
	 */
	private List<Double> parseArguments() {
	    List<Double> args = new ArrayList<>();
	    if (eat(')')) {
		return args;
	    }
	    do {
		args.add(parseExpression());
	    } while (eat(','));
	    if (!eat(')')) {
		throw new IllegalArgumentException("Missing closing parenthesis in function call");
	    }
	    return args;
	}

	private static double callFunction(String name, List<Double> args) {
	    switch (name.toUpperCase()) {
	    case "POWER":
	    case "POW":
		checkArgs(name, args, 2, 2);
		return Math.pow(args.get(0), args.get(1));
	    case "EXP":
		checkArgs(name, args, 1, 1);
		return Math.exp(args.get(0));
	    case "LN":
		checkArgs(name, args, 1, 1);
		return Math.log(args.get(0));
	    case "LOG10":
		checkArgs(name, args, 1, 1);
		return Math.log10(args.get(0));
	    case "LOG":
		// spreadsheet semantics: base 10 by default
		checkArgs(name, args, 1, 2);
		return args.size() == 1 ? Math.log10(args.get(0)) : Math.log(args.get(0)) / Math.log(args.get(1));
	    case "SQRT":
		checkArgs(name, args, 1, 1);
		return Math.sqrt(args.get(0));
	    case "ABS":
		checkArgs(name, args, 1, 1);
		return Math.abs(args.get(0));
	    default:
		throw new IllegalArgumentException("Unsupported function: " + name);
	    }
	}

	private static void checkArgs(String name, List<Double> args, int min, int max) {
	    if (args.size() < min || args.size() > max) {
		throw new IllegalArgumentException("Wrong number of arguments for " + name + ": " + args.size());
	    }
	}
    }
}
