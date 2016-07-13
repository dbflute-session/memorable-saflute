package ${packageName}.domainfw.action;

import ${packageName}.projectfw.web.action.${ProjectName}Messages;
import org.apache.struts.action.ActionMessage;

/**
 * The keys for message.
 * @author FreeGen
 */
public class ${AppName}Messages extends ${ProjectName}Messages {

    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    /** The key of the message: 例: 290-9753 */
    public static final String MESSAGES_ZIPCODE_INPUT_EXAMPLE = "messages.zipCode.input.example";

    /**
     * Add the created action message for the key 'errors.empty.login' with parameters.
     * <pre>
     * message: メールアドレスまたはパスワードをないがしろにしています
     * comment: @Override ----------------
     * </pre>
     * @param property The property name for the message. (NotNull)
     */
    @Override
    public void addErrorsEmptyLogin(String property) {
        assertPropertyNotNull(property);
        add(property, new ActionMessage(ERRORS_EMPTY_LOGIN, (Object[])null));
    }

    /**
     * Add the created action message for the key 'errors.not.login' with parameters.
     * <pre>
     * message: メールアドレスとパスワードが地球外のものです
     * comment: @Override
     * </pre>
     * @param property The property name for the message. (NotNull)
     */
    @Override
    public void addErrorsNotLogin(String property) {
        assertPropertyNotNull(property);
        add(property, new ActionMessage(ERRORS_NOT_LOGIN, (Object[])null));
    }

    /**
     * Add the created action message for the key 'messages.zipCode.input.example' with parameters.
     * <pre>
     * message: 例: 290-9753
     * comment: ----------
     * </pre>
     * @param property The property name for the message. (NotNull)
     */
    public void addMessagesZipcodeInputExample(String property) {
        assertPropertyNotNull(property);
        add(property, new ActionMessage(MESSAGES_ZIPCODE_INPUT_EXAMPLE, (Object[])null));
    }

    /**
     * The definition of keys for labels.
     * @author FreeGen
     */
    public static interface LabelKey extends ${ProjectName}Messages.LabelKey {

        /** The key of the label: 郵便番号 */
        String LABELS_ZIPCODE = "labels.zipCode";
    }
}
