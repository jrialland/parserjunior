package net.jr.parser.impl;

/**
 * Description of an action for the {@link ActionTable}
 *
 */
public class Action {

    private ActionType actionType;

    private int actionParameter;

    private ActionType Ac;

    public Action(ActionType actionType, int actionParameter) {
        this.actionType = actionType;
        this.actionParameter = actionParameter;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public int getActionParameter() {
        return actionParameter;
    }

    @Override
    public int hashCode() {
        return actionType.hashCode() + actionParameter;
    }

    @Override
    public boolean equals(Object o) {
        if(o==null||!o.getClass().equals(Action.class)) {
            return false;
        }
        final Action oAction = (Action)o;
        return oAction.actionType.equals(actionType) && oAction.actionParameter == actionParameter;
    }

    @Override
    public String toString() {
        if(actionType.equals(ActionType.Fail)) {
            return "-";
        } else {
            return (actionType.name().charAt(0) + Integer.toString(actionParameter)).toLowerCase();
        }
    }
}
