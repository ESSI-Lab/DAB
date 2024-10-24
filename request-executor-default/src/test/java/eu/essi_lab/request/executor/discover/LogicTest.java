package eu.essi_lab.request.executor.discover;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.formulas.Literal;
import org.logicng.formulas.Variable;

public class LogicTest {

    @Test
    public void test() {
	final FormulaFactory factory = new FormulaFactory();
	final Variable a = factory.variable("A");
	final Variable b = factory.variable("B");
	final Literal notC = factory.literal("C", false);
	final Formula formula = factory.and(a, factory.and(a, factory.or(factory.not(factory.or(b, notC)), notC)));

	Formula dnf = formula.bdd().dnf();

	System.out.println(dnf.toString());

	assertEquals("A & ~B & C | A & B & ~C | A & ~B & ~C", dnf.toString());

    }

}
