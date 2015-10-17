/*
 * The MIT License
 *
 * Copyright (c) 2009-, Kohsuke Kawaguchi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.kohsuke.metainf_services;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.kohsuke.MetaInfServices;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"Since15"})
@SupportedAnnotationTypes("org.kohsuke.MetaInfServices")
public class AnnotationProcessorImpl extends AbstractProcessor {

    @Override public SourceVersion getSupportedSourceVersion() {
        try {
            // Seems to work. Probably could use some additional error checks, but current code does not even verify that the class is assignable to an explicitly specified type!
            // Need to add unit tests. See stapler/stapler/core/src/test/java/org/kohsuke/stapler/jsr269/ for examples.
            return SourceVersion.valueOf("RELEASE_8");
        } catch (IllegalArgumentException x) {}
        try {
            return SourceVersion.valueOf("RELEASE_7");
        } catch (IllegalArgumentException x) {}
        return SourceVersion.RELEASE_6;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver())      return false;
        // TODO should not write anything until processingOver

        Map<String,Set<String>> services = new HashMap<String, Set<String>>();

        Elements elements = processingEnv.getElementUtils();

        // discover services from the current compilation sources
        for (Element e : roundEnv.getElementsAnnotatedWith(MetaInfServices.class)) {
            MetaInfServices a = e.getAnnotation(MetaInfServices.class);
            if(a==null) continue; // input is malformed, ignore
            if (!e.getKind().isClass() && !e.getKind().isInterface()) continue; // ditto
            TypeElement type = (TypeElement)e;
            Collection<TypeElement> contracts = getContracts(type, a);
            if(contracts.isEmpty())  continue; // error should have already been reported

            for (TypeElement contract : contracts) {

            String cn = elements.getBinaryName(contract).toString();
            Set<String> v = services.get(cn);
            if(v==null)
                services.put(cn,v=new TreeSet<String>());
            v.add(elements.getBinaryName(type).toString());

            }

        }

        // also load up any existing values, since this compilation may be partial
        Filer filer = processingEnv.getFiler();
        for (Map.Entry<String,Set<String>> e : services.entrySet()) {
            try {
                String contract = e.getKey();
                FileObject f = filer.getResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" +contract);
                BufferedReader r = new BufferedReader(new InputStreamReader(f.openInputStream(), "UTF-8"));
                String line;
                while((line=r.readLine())!=null)
                    e.getValue().add(line);
                r.close();
            } catch (FileNotFoundException x) {
                // doesn't exist
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR,"Failed to load existing service definition files: "+x);
            }
        }

        // now write them back out
        for (Map.Entry<String,Set<String>> e : services.entrySet()) {
            try {
                String contract = e.getKey();
                processingEnv.getMessager().printMessage(Kind.NOTE,"Writing META-INF/services/"+contract);
                FileObject f = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" +contract);
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(f.openOutputStream(), "UTF-8"));
                for (String value : e.getValue())
                    pw.println(value);
                pw.close();
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR,"Failed to write service definition files: "+x);
            }
        }

        return false;
    }

    private Collection<TypeElement> getContracts(TypeElement type, MetaInfServices a) {
        List<TypeElement> typeElementList = new ArrayList<TypeElement>();

        // explicitly specified?
        try {
            a.value();
            throw new AssertionError();
        } catch (MirroredTypesException e) {

          for (TypeMirror m : e.getTypeMirrors()) {
            if (m.getKind()== TypeKind.VOID) {
                // contract inferred from the signature
                boolean hasBaseClass = type.getSuperclass().getKind()!=TypeKind.NONE && !isObject(type.getSuperclass());
                boolean hasInterfaces = !type.getInterfaces().isEmpty();
                if(hasBaseClass^hasInterfaces) {
                    if(hasBaseClass)
                        typeElementList.add((TypeElement)((DeclaredType)type.getSuperclass()).asElement());
                    else
                        typeElementList.add((TypeElement)((DeclaredType)type.getInterfaces().get(0)).asElement());
                    continue;
                }

                error(type, "Contract type was not specified, but it couldn't be inferred.");
                continue;
            }

            if (m instanceof DeclaredType) {
                DeclaredType dt = (DeclaredType) m;
                typeElementList.add((TypeElement)dt.asElement());
                continue;
            } else {
                error(type, "Invalid type specified as the contract");
                continue;
            }
          }
        }
        return typeElementList;
    }

    private boolean isObject(TypeMirror t) {
        if (t instanceof DeclaredType) {
            DeclaredType dt = (DeclaredType) t;
            return((TypeElement)dt.asElement()).getQualifiedName().toString().equals("java.lang.Object");
        }
        return false;
    }

    private void error(Element source, String msg) {
        processingEnv.getMessager().printMessage(Kind.ERROR,msg,source);
    }
}
