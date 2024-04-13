javac -cp ../../../java-advanced-2023/artifacts/info.kgeorgiy.java.advanced.base.jar:../../../java-advanced-2023/artifacts/info.kgeorgiy.java.advanced.implementor.jar ../java-solutions/info/kgeorgiy/ja/ilyk/implementor/Implementor.java

jar -cfm ImplementorJar.jar META-INF/MANIFEST.MF -C ../java-solutions info/kgeorgiy/ja/ilyk/implementor/Implementor.class