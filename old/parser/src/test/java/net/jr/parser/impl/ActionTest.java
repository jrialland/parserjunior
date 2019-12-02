package net.jr.parser.impl;

import org.junit.Assert;
import org.junit.Test;

public class ActionTest {

    @Test
    public void testEquals() {
        Action action1 = new Action(ActionType.Fail, 1);
        Action action2 = new Action(ActionType.Fail, 2);
        Action action2prime = new Action(ActionType.Accept, 2);
        Action action3 = new Action(ActionType.Fail, 3);
        Assert.assertTrue(action1.equals(action1));
        Assert.assertFalse(action1.equals(action2));
        Assert.assertFalse(action2.equals(action2prime));
        Assert.assertFalse(action3.equals(null));
        Assert.assertFalse(action3.equals("action3"));
        action1.toString();
        action2.toString();
        action2prime.toString();
        action3.toString();
    }

    public void testHashCode() {
        Action action = new Action(ActionType.Accept, 0);
        Action action2 = new Action(ActionType.Accept, 0);
        Assert.assertEquals(action.hashCode(), action2.hashCode());
    }

}
