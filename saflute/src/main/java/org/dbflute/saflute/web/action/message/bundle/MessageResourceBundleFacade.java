package org.dbflute.saflute.web.action.message.bundle;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Properties;

import org.seasar.framework.container.hotdeploy.HotdeployUtil;
import org.seasar.framework.exception.IORuntimeException;
import org.seasar.framework.util.AssertionUtil;
import org.seasar.framework.util.FileInputStreamUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.framework.util.URLUtil;

/**
 * @author modified by jflute (originated in Seasar)
 */
public class MessageResourceBundleFacade {

    private File file;

    private long lastModified;

    private MessageResourceBundle bundle;

    private MessageResourceBundleFacade parent;

    public MessageResourceBundleFacade(URL url) {
        setup(url);
    }

    public synchronized MessageResourceBundle getBundle() {
        if (isModified()) {
            bundle = createBundle(file);
        }
        if (parent != null) {
            bundle.setParent(parent.getBundle());
        }
        return bundle;
    }

    public synchronized MessageResourceBundleFacade getParent() {
        return parent;
    }

    public synchronized void setParent(MessageResourceBundleFacade parent) {
        this.parent = parent;
    }

    protected boolean isModified() {
        if (file != null && file.lastModified() > lastModified) {
            return true;
        }
        return false;
    }

    protected void setup(URL url) {
        AssertionUtil.assertNotNull("url", url);
        if (HotdeployUtil.isHotdeploy()) {
            file = ResourceUtil.getFile(url);
        }
        if (file != null) {
            lastModified = file.lastModified();
            bundle = createBundle(file);
        } else {
            bundle = createBundle(url);
        }
        if (parent != null) {
            bundle.setParent(parent.getBundle());
        }
    }

    protected static MessageResourceBundle createBundle(File file) {
        return new MessageResourceBundleImpl(createProperties(file));
    }

    protected static MessageResourceBundle createBundle(URL url) {
        return new MessageResourceBundleImpl(createProperties(url));
    }

    protected static Properties createProperties(File file) {
        return createProperties(FileInputStreamUtil.create(file));
    }

    protected static Properties createProperties(URL url) {
        return createProperties(URLUtil.openStream(url));
    }

    protected static Properties createProperties(InputStream ins) { // *extension point
        AssertionUtil.assertNotNull("ins", ins);
        if (!(ins instanceof BufferedInputStream)) {
            ins = new BufferedInputStream(ins);
        }
        try {
            Properties prop = createProperties();
            loadProperties(prop, ins, getPropertiesEncoding());
            return prop;
        } finally {
            InputStreamUtil.close(ins);
        }
    }

    protected static Properties createProperties() {
        return new Properties();
    }

    protected static String getPropertiesEncoding() {
        return "UTF-8"; // as default
    }

    protected static void loadProperties(Properties properties, InputStream ins, String encoding) {
        try {
            load(properties, new BufferedReader(new InputStreamReader(ins, encoding)));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding: " + encoding);
        }
    }

    protected static void load(Properties props, Reader reader) throws IORuntimeException {
        try {
            props.load(reader);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }
}