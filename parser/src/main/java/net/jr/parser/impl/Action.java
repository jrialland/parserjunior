package net.jr.parser.impl;

import net.jr.marshalling.MarshallingCapable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Description of an action for the {@link ActionTable}
 */
public class Action implements MarshallingCapable {

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
        if (o == null || !o.getClass().equals(Action.class)) {
            return false;
        }
        final Action oAction = (Action) o;
        return oAction.actionType.equals(actionType) && oAction.actionParameter == actionParameter;
    }

    @Override
    public String toString() {
        if (actionType.equals(ActionType.Fail)) {
            return "-";
        } else {
            return (actionType.name().charAt(0) + Integer.toString(actionParameter)).toLowerCase();
        }
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(actionType.name());
        dataOutputStream.writeInt(actionParameter);
    }

    public static Action unMarshall(DataInputStream dataInputStream) throws IOException {
        ActionType actionType = ActionType.valueOf(dataInputStream.readUTF());
        int actionParameter = dataInputStream.readInt();
        return new Action(actionType, actionParameter);
    }
}
