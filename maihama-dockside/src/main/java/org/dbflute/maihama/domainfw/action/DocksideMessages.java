package org.dbflute.maihama.domainfw.action;

import org.dbflute.maihama.projectfw.web.action.MaihamaMessages;
import org.apache.struts.action.ActionMessage;

/**
 * The keys for message.
 * @author FreeGen
 */
public class DocksideMessages extends MaihamaMessages {

    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    /** The key of the message: 例: 290-9753 */
    public static final String MESSAGES_ZIP_CODE_INPUT_EXAMPLE = "messages.zipCode.input.example";

    /**
     * Add the created action message for the key 'errors.empty.login' with parameters.
     * <pre>
     * message: メールアドレスとか、まあそういうのないとこまる
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
     * message: メールとパスワード、どっちかやばい
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
    public void addMessagesZipCodeInputExample(String property) {
        assertPropertyNotNull(property);
        add(property, new ActionMessage(MESSAGES_ZIP_CODE_INPUT_EXAMPLE, (Object[])null));
    }

    /**
     * The definition of keys for labels.
     * @author FreeGen
     */
    public static interface LabelKey extends MaihamaMessages.LabelKey {

        /** The key of the label: ゆーびんばんごー */
        String LABELS_ZIP_CODE = "labels.zipCode";
    }
}
