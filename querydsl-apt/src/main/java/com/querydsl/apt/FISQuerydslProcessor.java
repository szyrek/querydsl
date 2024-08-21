/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.querydsl.apt;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.querydsl.core.annotations.*;

/**
 * Default annotation processor for Querydsl which handles {@link QueryEntity}, {@link QuerySupertype},
 * {@link QueryEmbeddable}, {@link QueryEmbedded} and {@link QueryTransient}
 *
 * @author tiwe
 *
 */
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes({"jakarta.annotation.Generated", "javax.annotation.processing.Generated", "javax.annotation.Generated"})
public class FISQuerydslProcessor extends AbstractProcessor {
    public FISQuerydslProcessor() {}
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("Running "+this.getClass().getSimpleName());
        try {
            return generateQClassesInitializer(roundEnv);
        } catch (IOException e) {
            System.err.println("Failed to generate QClassesInitializer: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private boolean canAccessType(String typeName) {
        return this.processingEnv
                .getElementUtils()
                .getTypeElement(typeName) != null;
    }

    private boolean canAccessEJBTypes() {
        return canAccessType("jakarta.ejb.Singleton") &&
                canAccessType("jakarta.ejb.Startup");
    }

    private List<String> getQClassesToInitialize(RoundEnvironment roundEnv) {
        return roundEnv.getElementsAnnotatedWith(FISGenerated.class)
                .stream()
                .map(this::getQClassName)
                .collect(Collectors.toList());
    }

    private String getQClassName(Element element) {
        String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        String simpleName = element.getSimpleName().toString();
        return (packageName.isEmpty() ? "" : ".") + simpleName;
    }

    private boolean generateQClassesInitializer(RoundEnvironment roundEnv) throws IOException {
        List<String> qClassesToInit = getQClassesToInitialize(roundEnv);
        if (!canAccessEJBTypes() || qClassesToInit.isEmpty()) {
            System.out.println("Nuthin to do");
            return false;
        }
        JavaFileObject loaderFile = processingEnv.getFiler().createSourceFile("com.hlag.fis.QClassesInitializer");
        try (Writer writer = loaderFile.openWriter()) {
            writer.write("package com.hlag.fis;\n\n");
            writer.write("import jakarta.ejb.Singleton;\n");
            writer.write("import jakarta.ejb.Startup;\n");
            writer.write("import javax.annotation.processing.Generated;\n\n");
            writer.write("@Singleton\n");
            writer.write("@Startup\n");
            writer.write("@Generated(\"FIS\")\n");
            writer.write("public class QClassesInitializer {\n");
            writer.write("\tpublic void tryLoad(String className) {\n");
            writer.write("\t\ttry {\n");
            writer.write("\t\t\tClass.forName(className);\n");
            writer.write("\t\t} catch (Exception e) {\n");
            writer.write("\t\t\te.printStackTrace();\n");
            writer.write("\t\t}\n");
            writer.write("\t}\n");
            writer.write("\tpublic EntityLoader() {\n");
            for (String qClassName : qClassesToInit) {
                writer.write("\t\ttryLoad(\"" + qClassName + "\");\n");
            }
            writer.write("\t}\n");
            writer.write("}\n");
        }
        System.out.println("Made some shit");
        return true;
    }
}
