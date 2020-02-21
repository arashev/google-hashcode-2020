package com.abcde;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

public class Utils {

    public static void main(String[] args) {
        List<String> names = new ArrayList<>();
        names.add("a_example.txt");
        names.add("b_read_on.txt");
        names.add("c_incunabula.txt");
        names.add("d_tough_choices.txt");
        names.add("e_so_many_books.txt");
        names.add("f_libraries_of_the_world.txt");

        for (String name : names) {
            UploadData data = read(name);
            List<Library> libraries = optimized6(data);
            write(name.replace(".txt", "_solution.txt"), libraries);
        }
    }

    // inner
    private static UploadData read(String name) {
        System.out.println("start reading " + name);
        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(Paths.get("./data/input/" + name)));
            Iterator<String> iterator = lines.iterator();
            String[] meta = iterator.next().split(" ");
            long booksAmount = Long.parseLong(meta[0]);
            long librariesAmount = Long.parseLong(meta[1]);
            long daysAvailable = Long.parseLong(meta[2]);

            Map<Long, Book> books = new HashMap<>();
            String[] booksLine = iterator.next().split(" ");
            for (int i = 0; i < booksAmount; i++) {
                books.put((long) i, new Book(i, Long.parseLong(booksLine[i])));
            }

            Set<Library> libraries = new HashSet<>();
            for (int i = 0; i < librariesAmount; i++) {
                String[] libraryMeta = iterator.next().split(" ");
                long daysToSignUp = Long.parseLong(libraryMeta[1]);
                long scanPerDay = Long.parseLong(libraryMeta[2]);
                Set<Book> libraryBooks = Arrays.stream(iterator.next().split(" "))
                                               .map(Long::parseLong)
                                               .map(books::get)
                                               .collect(toSet());
                libraries.add(new Library(i, libraryBooks, daysToSignUp, scanPerDay));
            }

            System.out.println("stop reading " + name);
            return new UploadData(libraries, new HashSet<>(books.values()), daysAvailable);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Library> naive(UploadData data) {
        return sortByScore(data.libraries);
    }

    private static List<Library> naive2(UploadData data) {
        List<Library> list = sortByScore(data.libraries);
        Set<Book> booksWeHave = new HashSet<>(new ArrayList<>(list.get(0).books));
        for (int i = 1; i < list.size(); i++) {
            Library library = list.get(i);
            library.books.removeAll(booksWeHave);
            booksWeHave.addAll(library.books);
        }
        list.removeIf(next -> next.books.isEmpty());
        return list;
    }

    private static List<Library> naive3(UploadData data) {
        System.out.println("start calculating");
        Set<Library> libraries = data.libraries;
        List<Library> result = new ArrayList<>();
        for (int i = 0; i < libraries.size(); i++) {
            List<Library> list = sortByScore(libraries);
            Library target = list.get(0);
            for (Library library : list) {
                library.books.removeAll(target.books);
            }
            libraries.removeIf(next -> next.books.isEmpty());
            libraries.removeIf(next -> next.id == target.id);
            result.add(target);
        }
        System.out.println("stop calculating");
        return result;
    }

    private static List<Library> optimized(UploadData data) {
        return data.libraries.stream()
                             .sorted(comparing(l -> ((Library) l).daysToSignUp).reversed())
                             .collect(toList());
    }

    private static List<Library> optimized2(UploadData data) {
        System.out.println("start forming index");
        Map<Book, Set<Library>> index = new HashMap<>();
        for (Library library : data.libraries) {
            for (Book book : library.books) {
                Set<Library> libraries = index.computeIfAbsent(book, k -> new HashSet<>());
                libraries.add(library);
            }
        }
        System.out.println("stop forming index");

        System.out.println("start calculating uq scores for libs");
        Map<Library, Integer> libScores = new HashMap<>();
        for (Library library : data.libraries) {
            Integer score = libScores.get(library);
            if (score == null) {
                score = 0;
            }
            for (Book book : library.books) {
                Set<Library> libraries = index.get(book);
                int occurrences = libraries.size();
                if (occurrences == 1) {
                    score += 1;
                }
            }
            libScores.put(library, score);
        }
        System.out.println("stop calculating uq scores for libs");

        System.out.println("start sorting by uq scores");
        List<Map.Entry<Library, Integer>> list =
                libScores.entrySet()
                         .stream()
                         .sorted(comparing(e -> ((Map.Entry<Library, Integer>) e).getValue()).reversed())
                         .collect(toList());
        System.out.println("stop sorting by uq scores");

        for (Map.Entry<Library, Integer> entry : list) {
            Library lib = entry.getKey();
            for (Book book : lib.books) {
                Set<Library> containingThisBook = index.get(book);
                for (Library library : containingThisBook) {
                    if (lib != library) {
                        library.books.remove(book);
                    }
                }
            }
        }
        List<Library> libraries = sortByScore(data.libraries);
        libraries.removeIf(library -> library.books.isEmpty());
        return libraries;
    }

    private static List<Library> optimized3(UploadData data) {
        System.out.println("start forming index");
        Map<Book, Set<Library>> index = new HashMap<>();
        for (Library library : data.libraries) {
            for (Book book : library.books) {
                Set<Library> libraries = index.computeIfAbsent(book, k -> new HashSet<>());
                libraries.add(library);
            }
        }
        System.out.println("stop forming index");

        System.out.println("start calculating uq scores for libs");
        Map<Library, List<Book>> libScores = new HashMap<>();
        for (Library library : data.libraries) {
            List<Book> score = libScores.get(library);
            if (score == null) {
                score = new ArrayList<>();
            }
            for (Book book : library.books) {
                Set<Library> libraries = index.get(book);
                int occurrences = libraries.size();
                if (occurrences == 1) {
                    score.add(book);
                }
            }
            libScores.put(library, score);
        }
        System.out.println("stop calculating uq scores for libs");

        System.out.println("start sorting by uq scores");
        List<Map.Entry<Library, List<Book>>> list =
                libScores.entrySet()
                         .stream()
                         .sorted(comparing(e -> ((Map.Entry<Library, List<Book>>) e).getValue().size()).reversed())
                         .collect(toList());
        System.out.println("stop sorting by uq scores");

        for (Map.Entry<Library, List<Book>> entry : list) {
            Library lib = entry.getKey();
            for (Book book : lib.books) {
                Set<Library> containingThisBook = index.get(book);
                for (Library library : containingThisBook) {
                    if (lib != library) {
                        library.books.remove(book);
                    }
                }
            }
        }
        List<Library> libraries = sortByScore2(data.libraries, libScores);
        libraries.removeIf(library -> library.books.isEmpty());
        return libraries;
    }

    private static List<Library> optimized4(UploadData data) {
        System.out.println("start forming index");
        Map<Book, Set<Library>> index = new HashMap<>();
        for (Library library : data.libraries) {
            for (Book book : library.books) {
                Set<Library> libraries = index.computeIfAbsent(book, k -> new HashSet<>());
                libraries.add(library);
            }
        }
        System.out.println("stop forming index");

        System.out.println("start calculating uq scores for libs");
        Map<Library, Double> libScores = new HashMap<>();
        for (Library library : data.libraries) {
            Double score = libScores.get(library);
            if (score == null) {
                score = 0.0;
            }
            for (Book book : library.books) {
                Set<Library> libraries = index.get(book);
                int occurrences = libraries.size();
                score += 1.0 / occurrences;
            }
            libScores.put(library, score);
        }
        System.out.println("stop calculating uq scores for libs");

        System.out.println("start sorting by uq scores");
        List<Map.Entry<Library, Double>> list =
                libScores.entrySet()
                         .stream()
                         .sorted(comparing(e -> ((Map.Entry<Library, Double>) e).getValue()).reversed())
                         .collect(toList());
        System.out.println("stop sorting by uq scores");

        for (Map.Entry<Library, Double> entry : list) {
            Library lib = entry.getKey();
            for (Book book : lib.books) {
                Set<Library> containingThisBook = index.get(book);
                for (Library library : containingThisBook) {
                    if (lib != library) {
                        library.books.remove(book);
                    }
                }
            }
        }
        List<Library> libraries = sortByScore(data.libraries);
        libraries.removeIf(library -> library.books.isEmpty());
        return libraries;
    }

    private static List<Library> optimized5(UploadData data) {
        System.out.println("start forming index");
        Map<Book, Set<Library>> index = new HashMap<>();
        for (Library library : data.libraries) {
            for (Book book : library.books) {
                Set<Library> libraries = index.computeIfAbsent(book, k -> new HashSet<>());
                libraries.add(library);
            }
        }
        System.out.println("stop forming index");

        System.out.println("start calculating uq scores for libs");
        Map<Library, Double> libScores = new HashMap<>();
        for (Library library : data.libraries) {
            Double score = libScores.get(library);
            if (score == null) {
                score = 0.0;
            }
            for (Book book : library.books) {
                Set<Library> libraries = index.get(book);
                int occurrences = libraries.size();
                score += 1.0 / occurrences;
            }
            libScores.put(library, score);
        }
        System.out.println("stop calculating uq scores for libs");

        System.out.println("start sorting by uq scores");
        List<Map.Entry<Library, Double>> list =
                libScores.entrySet()
                         .stream()
                         .sorted(comparing(e -> ((Map.Entry<Library, Double>) e).getValue()).reversed())
                         .collect(toList());
        System.out.println("stop sorting by uq scores");

        for (Map.Entry<Library, Double> entry : list) {
            Library lib = entry.getKey();
            for (Book book : lib.books) {
                Set<Library> containingThisBook = index.get(book);
                for (Library library : containingThisBook) {
                    if (lib != library) {
                        library.books.remove(book);
                    }
                }
            }
        }
        List<Library> libraries = sortByScore3(data.libraries, data.availableDays);
        libraries.removeIf(library -> library.books.isEmpty());
        return libraries;
    }

    private static List<Library> optimized6(UploadData data) {
        System.out.println("start forming index");
        Map<Book, Set<Library>> index = new HashMap<>();
        for (Library library : data.libraries) {
            for (Book book : library.books) {
                Set<Library> libraries = index.computeIfAbsent(book, k -> new HashSet<>());
                libraries.add(library);
            }
        }
        System.out.println("stop forming index");

        System.out.println("start calculating uq scores for libs");
        Map<Library, Double> libScores = new HashMap<>();
        for (Library library : data.libraries) {
            Double score = libScores.get(library);
            if (score == null) {
                score = 0.0;
            }
            for (Book book : library.books) {
                Set<Library> libraries = index.get(book);
                int occurrences = libraries.size();
                score += ((double) book.score) / occurrences;
            }
            libScores.put(library, score);
        }
        System.out.println("stop calculating uq scores for libs");

        System.out.println("start sorting by uq scores");
        List<Map.Entry<Library, Double>> list =
                libScores.entrySet()
                         .stream()
                         .sorted(comparing(e -> ((Map.Entry<Library, Double>) e).getValue()).reversed())
                         .collect(toList());
        System.out.println("stop sorting by uq scores");

        for (Map.Entry<Library, Double> entry : list) {
            Library lib = entry.getKey();
            for (Book book : lib.books) {
                Set<Library> containingThisBook = index.get(book);
                for (Library library : containingThisBook) {
                    if (lib != library) {
                        library.books.remove(book);
                    }
                }
            }
        }
        List<Library> libraries = sortByScore3(data.libraries, data.availableDays);
        libraries.removeIf(library -> library.books.isEmpty());
        return libraries;
    }

    private static List<Library> sortByScore(Collection<Library> libraries) {
        return libraries.stream()
                        .sorted(comparing(Library::getScore).reversed())
                        .collect(toList());
    }

    private static List<Library> sortByScore2(Collection<Library> libraries, Map<Library, List<Book>> libScores) {
        return libraries.stream()
                        .sorted(new Comparator<Library>() {
                            @Override
                            public int compare(Library o1, Library o2) {
                                return Double.compare(o1.getScore2(libScores.get(o1)), o2.getScore2(libScores.get(o2)));
                            }
                        })
                        .collect(toList());
    }

    private static List<Library> sortByScore3(Collection<Library> libraries, long availableDays) {
        return libraries.stream()
                        .sorted(comparing(l -> {
                            Library l1 = (Library) l;
                            double score = l1.getScore();
                            double signUpPercentage = ((double) l1.daysToSignUp) / availableDays;
                            if (signUpPercentage > 1.0) {
                                return -9999999999.0;
                            }

                            return score / signUpPercentage;
                        }).reversed())
                        .collect(toList());
    }

    private static void write(String name, List<Library> libraries) {
        System.out.println("start writing " + name);
        StringBuilder sb = new StringBuilder();
        sb.append(libraries.size()).append("\n");
        for (Library library : libraries) {
            sb.append(library.id).append(" ").append(library.books.size()).append("\n");
            sb.append(library.books.stream().sorted(comparing(book -> ((Book) book).score).reversed()).map(book -> Long.toString(book.id)).collect(joining(" "))).append("\n");
        }

        try {
            Files.write(Paths.get("./data/output/" + name), sb.toString().getBytes(UTF_8), CREATE, WRITE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("stop writing " + name);
    }
}
