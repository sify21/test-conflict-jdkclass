package javax.xml.xpath;

import java.net.*;
import java.io.*;
import java.security.*;
import java.util.*;

final class XPathFactoryFinder
{
    private static boolean debug;
    private static final int DEFAULT_LINE_LENGTH = 80;
    private static Properties cacheProps;
    private static boolean firstTime;
    private final ClassLoader classLoader;
    private static final Class SERVICE_CLASS;
    private static final String SERVICE_ID;
    static /* synthetic */ Class class$javax$xml$xpath$XPathFactory;
    private static final SecuritySupport securitySupport = new SecuritySupport();
    
    private static void debugPrintln(final String str) {
        if (XPathFactoryFinder.debug) {
            System.err.println("JAXP: " + str);
        }
    }
    
    public XPathFactoryFinder(final ClassLoader classLoader) {
        super();
        this.classLoader = classLoader;
        if (XPathFactoryFinder.debug) {
            this.debugDisplayClassLoader();
        }
    }
    
    private void debugDisplayClassLoader() {
        try {
            if (this.classLoader == securitySupport.getContextClassLoader()) {
                debugPrintln("using thread context class loader (" + this.classLoader + ") for search");
                return;
            }
        }
        catch (final VirtualMachineError virtualMachineError) {
            throw virtualMachineError;
        }
        catch (final ThreadDeath threadDeath) {
            throw threadDeath;
        }
        catch (final Throwable t) {}
        if (this.classLoader == ClassLoader.getSystemClassLoader()) {
            debugPrintln("using system class loader (" + this.classLoader + ") for search");
            return;
        }
        debugPrintln("using class loader (" + this.classLoader + ") for search");
    }
    
    public XPathFactory newFactory(final String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        final XPathFactory newFactory = this._newFactory(s);
        if (XPathFactoryFinder.debug) {
            if (newFactory != null) {
                debugPrintln("factory '" + newFactory.getClass().getName() + "' was found for " + s);
            }
            else {
                debugPrintln("unable to find a factory for " + s);
            }
        }
        return newFactory;
    }
    
    private XPathFactory _newFactory(final String str) {
        final String string = XPathFactoryFinder.SERVICE_CLASS.getName() + ":" + str;
        try {
            if (XPathFactoryFinder.debug) {
                debugPrintln("Looking up system property '" + string + "'");
            }
            final String systemProperty = securitySupport.getSystemProperty(string);
            if (systemProperty != null && systemProperty.length() > 0) {
                if (XPathFactoryFinder.debug) {
                    debugPrintln("The value is '" + systemProperty + "'");
                }
                final XPathFactory instance = this.createInstance(systemProperty);
                if (instance != null) {
                    return instance;
                }
            }
            else if (XPathFactoryFinder.debug) {
                debugPrintln("The property is undefined.");
            }
        }
        catch (final VirtualMachineError virtualMachineError) {
            throw virtualMachineError;
        }
        catch (final ThreadDeath threadDeath) {
            throw threadDeath;
        }
        catch (final Throwable t) {
            if (XPathFactoryFinder.debug) {
                debugPrintln("failed to look up system property '" + string + "'");
                t.printStackTrace();
            }
        }
        final String string2 = securitySupport.getSystemProperty("java.home") + File.separator + "lib" + File.separator + "jaxp.properties";
        try {
            if (XPathFactoryFinder.firstTime) {
                synchronized (XPathFactoryFinder.cacheProps) {
                    if (XPathFactoryFinder.firstTime) {
                        final File obj = new File(string2);
                        XPathFactoryFinder.firstTime = false;
                        if (securitySupport.doesFileExist(obj)) {
                            if (XPathFactoryFinder.debug) {
                                debugPrintln("Read properties file " + obj);
                            }
                            XPathFactoryFinder.cacheProps.load(securitySupport.getFileInputStream(obj));
                        }
                    }
                }
            }
            final String property = XPathFactoryFinder.cacheProps.getProperty(string);
            if (XPathFactoryFinder.debug) {
                debugPrintln("found " + property + " in $java.home/jaxp.properties");
            }
            if (property != null) {
                final XPathFactory instance2 = this.createInstance(property);
                if (instance2 != null) {
                    return instance2;
                }
            }
        }
        catch (final Exception ex) {
            if (XPathFactoryFinder.debug) {
                ex.printStackTrace();
            }
        }
        final Iterator serviceFileIterator = this.createServiceFileIterator();
        while (serviceFileIterator.hasNext()) {
            final URL url = (URL) serviceFileIterator.next();
            if (XPathFactoryFinder.debug) {
                debugPrintln("looking into " + url);
            }
            try {
                final XPathFactory loadFromServicesFile = this.loadFromServicesFile(str, url.toExternalForm(), securitySupport.getURLInputStream(url));
                if (loadFromServicesFile != null) {
                    return loadFromServicesFile;
                }
                continue;
            }
            catch (final IOException ex2) {
                if (!XPathFactoryFinder.debug) {
                    continue;
                }
                debugPrintln("failed to read " + url);
                ex2.printStackTrace();
            }
        }
        if (str.equals("http://java.sun.com/jaxp/xpath/dom")) {
            if (XPathFactoryFinder.debug) {
                debugPrintln("attempting to use the platform default W3C DOM XPath lib");
            }
            return this.createInstance("org.apache.xpath.jaxp.XPathFactoryImpl");
        }
        if (XPathFactoryFinder.debug) {
            debugPrintln("all things were tried, but none was found. bailing out.");
        }
        return null;
    }
    
    XPathFactory createInstance(final String str) {
        try {
            if (XPathFactoryFinder.debug) {
                debugPrintln("instanciating " + str);
            }
            Class<?> clazz;
            if (this.classLoader != null) {
                clazz = this.classLoader.loadClass(str);
            }
            else {
                clazz = Class.forName(str);
            }
            if (XPathFactoryFinder.debug) {
                debugPrintln("loaded it from " + which(clazz));
            }
            final Object instance = clazz.newInstance();
            if (instance instanceof XPathFactory) {
                return (XPathFactory)instance;
            }
            if (XPathFactoryFinder.debug) {
                debugPrintln(str + " is not assignable to " + XPathFactoryFinder.SERVICE_CLASS.getName());
            }
        }
        catch (final VirtualMachineError virtualMachineError) {
            throw virtualMachineError;
        }
        catch (final ThreadDeath threadDeath) {
            throw threadDeath;
        }
        catch (final Throwable t) {
            if (XPathFactoryFinder.debug) {
                debugPrintln("failed to instanciate " + str);
                t.printStackTrace();
            }
        }
        return null;
    }
    
    private XPathFactory loadFromServicesFile(final String s, final String str, final InputStream inputStream) {
        if (XPathFactoryFinder.debug) {
            debugPrintln("Reading " + str);
        }
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 80);
        }
        catch (final UnsupportedEncodingException ex) {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 80);
        }
        XPathFactory xPathFactory = null;
        while (true) {
            String s2;
            try {
                s2 = bufferedReader.readLine();
            }
            catch (final IOException ex2) {
                break;
            }
            if (s2 == null) {
                break;
            }
            final int index = s2.indexOf(35);
            if (index != -1) {
                s2 = s2.substring(0, index);
            }
            final String trim = s2.trim();
            if (trim.length() == 0) {
                continue;
            }
            try {
                final XPathFactory instance = this.createInstance(trim);
                if (instance.isObjectModelSupported(s)) {
                    xPathFactory = instance;
                    break;
                }
                continue;
            }
            catch (final Exception ex3) {}
        }
        try {
            bufferedReader.close();
        }
        catch (final IOException ex4) {}
        return xPathFactory;
    }
    
    private Iterator createServiceFileIterator() {
            if (XPathFactoryFinder.debug) {
                debugPrintln("failed to enumerate resources " + XPathFactoryFinder.SERVICE_ID);
            }
            return new ArrayList().iterator();
    }
    
    private static String which(final Class clazz) {
        return which(clazz.getName(), clazz.getClassLoader());
    }
    
    private static String which(final String s, ClassLoader systemClassLoader) {
        final String string = s.replace('.', '/') + ".class";
        if (systemClassLoader == null) {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        }
        final URL resourceAsURL = securitySupport.getResourceAsURL(systemClassLoader, string);
        if (resourceAsURL != null) {
            return resourceAsURL.toString();
        }
        return null;
    }
    
    static /* synthetic */ String access$100() {
        return XPathFactoryFinder.SERVICE_ID;
    }
    
    static /* synthetic */ Class class$(final String className) {
        try {
            return Class.forName(className);
        }
        catch (final ClassNotFoundException ex) {
            throw new NoClassDefFoundError(ex.getMessage());
        }
    }
    
    static {
        XPathFactoryFinder.debug = false;
        try {
            final String systemProperty = securitySupport.getSystemProperty("jaxp.debug");
            XPathFactoryFinder.debug = (systemProperty != null && !"false".equals(systemProperty));
        }
        catch (final Exception ex) {
            XPathFactoryFinder.debug = false;
        }
        XPathFactoryFinder.cacheProps = new Properties();
        XPathFactoryFinder.firstTime = true;
        SERVICE_CLASS = XPathFactory.class;
        SERVICE_ID = "META-INF/services/" + XPathFactoryFinder.SERVICE_CLASS.getName();
    }
}
