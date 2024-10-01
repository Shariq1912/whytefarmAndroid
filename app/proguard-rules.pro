# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class org.json.**
-keepclassmembers class org.json.** {*;}

-keep class com.payu.**
-keepclassmembers class com.payu.** {*;}

-dontwarn com.payu.cardscanner.PayU
-dontwarn com.payu.cardscanner.callbacks.PayUCardListener
-dontwarn com.payu.olamoney.OlaMoney
-dontwarn com.payu.olamoney.callbacks.OlaMoneyCallback
-dontwarn com.payu.olamoney.utils.PayUOlaMoneyParams
-dontwarn com.payu.olamoney.utils.PayUOlaMoneyPaymentParams
-dontwarn com.payu.phonepe.PhonePe
-dontwarn com.payu.phonepe.callbacks.PayUPhonePeCallback
-dontwarn com.payu.cardscanner.model.PUCardRecognizer
-dontwarn com.payu.cardscanner.model.PUError
-dontwarn com.payu.cardscanner.model.PUTextRecognizer


-dontwarn groovy.lang.GroovyObject
-dontwarn groovy.lang.MetaClass
-dontwarn java.lang.management.ManagementFactory
-dontwarn javax.management.InstanceNotFoundException
-dontwarn javax.management.MBeanRegistrationException
-dontwarn javax.management.MBeanServer
-dontwarn javax.management.MalformedObjectNameException
-dontwarn javax.management.ObjectInstance
-dontwarn javax.management.ObjectName
-dontwarn javax.servlet.ServletContainerInitializer
-dontwarn kotlinx.parcelize.Parcelize
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.codehaus.commons.compiler.CompileException
-dontwarn org.codehaus.groovy.reflection.ClassInfo
-dontwarn org.codehaus.groovy.runtime.BytecodeInterface8
-dontwarn org.codehaus.groovy.runtime.ScriptBytecodeAdapter
-dontwarn org.codehaus.groovy.runtime.callsite.CallSite
-dontwarn org.codehaus.groovy.runtime.callsite.CallSiteArray
-dontwarn org.codehaus.janino.ClassBodyEvaluator
-dontwarn org.conscrypt.Conscrypt
-dontwarn sun.reflect.Reflection