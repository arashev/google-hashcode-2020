package com.abcde;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class Library {

    public final long id;
    public final List<Book> books;
    public final long daysToSignUp;
    public final long scannedPerDay;

    public Library(long id, Set<Book> books, long daysToSignUp, long scannedPerDay) {
        this.id = id;
        this.books = books.stream().sorted(Comparator.comparing(book -> ((Book) book).score).reversed()).collect(Collectors.toList());
        this.daysToSignUp = daysToSignUp;
        this.scannedPerDay = scannedPerDay;
    }

    public double getScore() {
        double sum = books.stream().mapToLong(book -> book.score).sum();
        double throughput = Math.ceil(((double) books.size()) / scannedPerDay);
        return sum / (daysToSignUp + throughput);
    }

    @Override
    public String toString() {
        return "Library{" +
               "id=" + id +
               ", books=" + books +
               ", daysToSignUp=" + daysToSignUp +
               ", scannedPerDay=" + scannedPerDay +
               '}';
    }

    public double getScore2(List<Book> uniqueBooks) {
        double sum = uniqueBooks.stream().mapToLong(book -> book.score).sum();
        double throughput = Math.ceil(((double) uniqueBooks.size()) / scannedPerDay);
        return sum / (daysToSignUp + throughput);
    }
}
