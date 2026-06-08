package eu.essi_lab.model.ratings;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A single piece of a piece-wise rating-curve {@link Formula}: a water-level validity range together with the
 * discharge sub-formula {@code Q = f(h)} that applies within it.
 * <p>
 * The sub-formula is a mathematical expression in the variable {@code h} (water level) supporting {@code + - * / ^},
 * parentheses and unary signs. Discharge is obtained through {@link #evaluate(double)}.
 *
 * @author boldrini
 */
public class FormulaRange {

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

	String f = formula.replace(',', '.').trim();
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
		x = Double.parseDouble(expression.substring(startPos, pos));
	    } else if (ch == 'h' || ch == 'H') {
		nextChar();
		x = h;
	    } else {
		throw new IllegalArgumentException("Unexpected character: " + (char) ch);
	    }

	    if (eat('^')) {
		x = Math.pow(x, parseFactor());
	    }
	    return x;
	}
    }
}
