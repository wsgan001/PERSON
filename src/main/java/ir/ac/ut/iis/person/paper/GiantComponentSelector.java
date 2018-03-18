/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.ac.ut.iis.person.paper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shayan
 */
public class GiantComponentSelector {

    Set<Long> giant;

    private static void pruneAuthorsGiant(String papersFile, String authorsFile) {
        try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("temp.txt")))) {
            Set<Long> authorsSet = new HashSet<>();
            try (Scanner pf = new Scanner(new BufferedInputStream(new FileInputStream(papersFile))); Scanner af = new Scanner(new BufferedInputStream(new FileInputStream(authorsFile)))) {
                pf.useDelimiter("\n");
                while (pf.hasNext()) {
                    Paper readPaper = readPaper(pf);
                    for (Long author : readPaper.authors) {
                        authorsSet.add(author);
                    }
                }
                af.useDelimiter("\n");

                while (af.hasNext()) {
                    String nextLine = af.next();
                    Long id = Long.parseLong(nextLine);
                    String name = af.next();
                    String rels = af.next();
                    if (!authorsSet.contains(id)) {
                        continue;
                    }

                    String conv = "";
                    if (!rels.isEmpty()) {
                        for (String ref : rels.split(",")) {
                            String[] split = ref.split(" ");
                            if (authorsSet.contains(Long.parseLong(split[0]))) {
                                conv += split[0] + " " + split[1] + ",";
                            }
                        }
                    }
                    writer.write(id + "\n");
                    writer.write(name + "\n");
                    writer.write(conv + "\n");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GiantComponentSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void convertPapersFile(String papersFile, String outputFileName) {
        int i = 0;
        Set<Long> giantDocs = new HashSet<>();
        final String tempName = outputFileName + ".tmp";
        try (Scanner sc = new Scanner(new BufferedInputStream(new FileInputStream(papersFile)))) {
            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(tempName)))) {
                sc.useDelimiter("\n");
                while (sc.hasNext()) {
                    i++;
                    Paper readPaper = readPaper(sc);
                    if (readPaper.next.equals("null")) {
                        readPaper.next = "";
                    }
                    if (readPaper.abs.length() + readPaper.title.length() < 150) {
                        throw new RuntimeException();
                    }

                    boolean ch = false;
                    String conv = "";
                    if (!readPaper.authors.isEmpty()) {
                        for (Long author : readPaper.authors) {
                            if (giant.contains(author)) {
                                ch = true;
                                conv += author + ",";
                            }
                        }
                    } else {
                        throw new RuntimeException();
                    }
                    if (ch) {
                        giantDocs.add(readPaper.id);
                        write(writer, readPaper.getIdAsString(), readPaper.isMerged, readPaper.docId, readPaper.identifier, readPaper.source, readPaper.uri, readPaper.lang, readPaper.title, readPaper.abs, readPaper.next, readPaper.next1, readPaper.next2, readPaper.getRefsAsString(), conv);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GiantComponentSelector.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException();
        }

        final String temp2Name = outputFileName + ".tmp2";
        try (Scanner sc = new Scanner(new BufferedInputStream(new FileInputStream(tempName)))) {
            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(temp2Name)))) {
                sc.useDelimiter("\n");
                while (sc.hasNext()) {
                    i++;
                    Paper readPaper = readPaper(sc);
                    if (readPaper.next.equals("null")) {
                        readPaper.next = "";
                    }

                    String conv = "";
                    if (!readPaper.refs.isEmpty()) {
                        for (Long ref : readPaper.refs) {
                            if (giantDocs.contains(ref)) {
                                conv += ref + ",";
                            }
                        }
                    }
                    write(writer, readPaper.getIdAsString(), readPaper.isMerged, readPaper.docId, readPaper.identifier, readPaper.source, readPaper.uri, readPaper.lang, readPaper.title, readPaper.abs, readPaper.next, readPaper.next1, readPaper.next2, conv, readPaper.getAuthorsAsString());
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GiantComponentSelector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GiantComponentSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
        new File(tempName).delete();
        String prunePapers = prunePapers(temp2Name);
        new File(prunePapers).renameTo(new File(outputFileName));
    }

    private void write(final Writer writer, String nextLine, Integer isMerged, String docId, String identifier, String source, String uri, String lang, String title, String abs, String next, String next1, String next2, String refs, String authors) throws IOException {
        writer.write(nextLine + "\n");
        writer.write(isMerged + "\n");
        writer.write(docId + "\n");
        writer.write(identifier + "\n");
        writer.write(source + "\n");
        writer.write(uri + "\n");
        writer.write(lang + "\n");
        writer.write(title + "\n");
        writer.write(abs + "\n");
        writer.write(next + "\n");
        writer.write(next1 + "\n");
        writer.write(next2 + "\n");
        writer.write(refs + "\n");
        writer.write(authors + "\n");
    }

    public void convertAuthorsFile(String authorsFile, String outputFileName, Set<Long> authorsSet) {
        int i = 0;
        try (Scanner sc = new Scanner(new BufferedInputStream(new FileInputStream(authorsFile)))) {
            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(outputFileName)))) {
                sc.useDelimiter("\n");

                while (sc.hasNext()) {
                    i++;
                    String nextLine = sc.next();
                    Long id = Long.parseLong(nextLine);
                    String name = sc.next();
                    String rels = sc.next();
                    if (giant.contains(id) && authorsSet.contains(id)) {
                        boolean ch = false;
                        String conv = "";
                        if (!rels.isEmpty()) {
                            for (String ref : rels.split(",")) {
                                String[] split = ref.split(" ");
                                final long parseLong = Long.parseLong(split[0]);
                                if (giant.contains(parseLong) && authorsSet.contains(parseLong)) {
                                    ch = true;
                                    conv += split[0] + " " + split[1] + ",";
                                }
                            }
                        }

                        if (ch) {
                            writer.write(id + "\n");
                            writer.write(name + "\n");
                            writer.write(conv + "\n");
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GiantComponentSelector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GiantComponentSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        new GiantComponentSelector().extractGiantCompnent();
//        pruneAuthorsGiant("papers_giant.txt", "authors_giant.txt");
    }

    private String prunePapers(String temp2Name) {
        int i = 3;
        String currentTemp = temp2Name;
        String nextTemp = temp2Name + "_" + i;
        boolean check;
        do {
            check = false;
            Set<Long> referencedPapers = new HashSet<>();
            try (Scanner sc = new Scanner(new BufferedInputStream(new FileInputStream(currentTemp)))) {
                sc.useDelimiter("\n");
                while (sc.hasNext()) {
                    Paper readPaper = readPaper(sc);
                    if (!readPaper.refs.isEmpty()) {
                        for (Long ref : readPaper.refs) {
                            referencedPapers.add(ref);
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GiantComponentSelector.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException();
            }
            try (Scanner sc = new Scanner(new BufferedInputStream(new FileInputStream(currentTemp)))) {
                try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(nextTemp)))) {
                    sc.useDelimiter("\n");
                    while (sc.hasNext()) {
                        Paper readPaper = readPaper(sc);
                        if (readPaper.next.equals("null")) {
                            readPaper.next = "";
                        }

                        if ((!readPaper.refs.isEmpty() && readPaper.refs.size() > 5) || referencedPapers.contains(readPaper.id)) {
                            write(writer, readPaper.getIdAsString(), readPaper.isMerged, readPaper.docId, readPaper.identifier, readPaper.source, readPaper.uri, readPaper.lang, readPaper.title, readPaper.abs, readPaper.next, readPaper.next1, readPaper.next2, readPaper.getRefsAsString(), readPaper.getAuthorsAsString());
                        } else {
                            check = true;
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GiantComponentSelector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GiantComponentSelector.class.getName()).log(Level.SEVERE, null, ex);
            }
            i++;
            new File(currentTemp).delete();
            currentTemp = nextTemp;
            nextTemp = temp2Name + "_" + i;
        } while (check);

        return currentTemp;
    }

    private void extractGiantCompnent() throws NumberFormatException {
        String authorsFileName = "authors.txt";
        String papersFileName = "papers.txt";
        int i = 0;
        while (true) {
            final Map<Long, Set<Long>> readGraphFile = readGraphFile(authorsFileName);
            giant = getGiantComponent(readGraphFile);
            if (giant.size() == readGraphFile.size()) {
                break;
            }
            convertPapersFile(papersFileName, "papers_giant.txt.tmp3_" + i);
            Set<Long> authorsSet = new HashSet<>();
            try (Scanner sc = new Scanner(new BufferedInputStream(new FileInputStream("papers_giant.txt.tmp3_" + i)))) {
                sc.useDelimiter("\n");
                while (sc.hasNext()) {
                    Paper readPaper = readPaper(sc);
                    for (Long author : readPaper.authors) {
                        authorsSet.add(author);
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GiantComponentSelector.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException();
            }

            convertAuthorsFile(authorsFileName, "authors_giant.txt.tmp3_" + i, authorsSet);
            if (i > 0) {
                new File(papersFileName).delete();
                new File(authorsFileName).delete();
            }
            papersFileName = "papers_giant.txt.tmp3_" + i;
            authorsFileName = "authors_giant.txt.tmp3_" + i;
            i++;
        }
        new File(papersFileName).renameTo(new File("papers_giant.txt"));
        new File(authorsFileName).renameTo(new File("authors_giant.txt"));
    }

    private static class Paper {

        long id;
        int isMerged;
        String docId;
        String identifier;
        String source;
        String uri;
        String lang;
        String title;
        String abs;
        String next;
        String next1;
        String next2;
        Set<Long> refs;
        Set<Long> authors;

        String getIdAsString() {
            return Long.toString(id);
        }

        String getRefsAsString() {
            StringBuilder sb = new StringBuilder();
            for (Long l : refs) {
                sb.append(l).append(",");
            }
            return sb.toString();
        }

        String getAuthorsAsString() {
            StringBuilder sb = new StringBuilder();
            for (Long l : authors) {
                sb.append(l).append(",");
            }
            return sb.toString();
        }

    }

    private static Paper readPaper(Scanner sc) {
        Paper paper = new Paper();
        paper.id = Long.parseLong(sc.next());
        paper.isMerged = sc.nextInt();
        paper.docId = sc.next();
        paper.identifier = sc.next();
        paper.source = sc.next();
        paper.uri = sc.next();
        paper.lang = sc.next();
        paper.title = sc.next();
        paper.abs = sc.next();
        paper.next = sc.next();
        paper.next1 = sc.next();
        paper.next2 = sc.next();
        String refsString = sc.next();
        Set<Long> refs = new HashSet<>();
        if (!refsString.isEmpty()) {
            for (String ref : refsString.split(",")) {
                refs.add(Long.parseLong(ref));
            }
        }
        paper.refs = refs;
        String authorsString = sc.next();
        Set<Long> authors = new HashSet<>();
        for (String author : authorsString.split(",")) {
            authors.add(Long.parseLong(author));
        }
        paper.authors = authors;
        return paper;
    }

    public Set<Long> getGiantComponent(Map<Long, Set<Long>> graph) {
        Map<Long, Integer> visited = new HashMap<>();
        int compId = 0;
        for (Long id : graph.keySet()) {
            if (!visited.containsKey(id)) {
                bfs(id, compId, visited, graph);
                compId++;
            }
        }
        Map<Integer, Integer> counts = new HashMap<>();
        int max = 0;
        int maxId = 0;
        for (Map.Entry<Long, Integer> e : visited.entrySet()) {
            Integer get = counts.get(e.getValue());
            if (get == null) {
                get = 0;
            }
            get++;
            if (get > max) {
                max = get;
                maxId = e.getValue();
            }
            counts.put(e.getValue(), get);
        }
        Set<Long> giantComponent = new HashSet<>();
        for (Map.Entry<Long, Integer> e : visited.entrySet()) {
            if (e.getValue().equals(maxId)) {
                giantComponent.add(e.getKey());
            }
        }
        return giantComponent;
    }

    public Map<Long, Set<Long>> readGraphFile(String authorsFile) throws NumberFormatException {
        Map<Long, Set<Long>> graph = new HashMap<>();
        try (Scanner af = new Scanner(new BufferedInputStream(new FileInputStream(authorsFile)))) {
            af.useDelimiter("\n");

            while (af.hasNext()) {
                Long id = Long.parseLong(af.next());
                String name = af.next();
                String rels = af.next();

                if (!rels.isEmpty()) {
                    for (String ref : rels.split(",")) {
                        Long l = Long.parseLong(ref.split(" ")[0]);
                        Set<Long> get = graph.get(l);
                        if (get == null) {
                            get = new HashSet<>();
                            graph.put(l, get);
                        }
                        get.add(id);
                        get = graph.get(id);
                        if (get == null) {
                            get = new HashSet<>();
                            graph.put(id, get);
                        }
                        get.add(l);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GiantComponentSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return graph;
    }

    public void bfs(Long src, int compId, Map<Long, Integer> visited, Map<Long, Set<Long>> graph) {
        LinkedList<Long> list = new LinkedList<>();
        list.add(src);
        visited.put(src, compId);
        while (!list.isEmpty()) {
            Long currentnode = list.removeFirst();
            for (Long n : graph.get(currentnode)) {
                if (!visited.containsKey(n)) {
                    visited.put(n, compId);
                    list.add(n);
                }
            }
        }
    }
}
