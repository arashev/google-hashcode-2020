package com.abcde;

import java.util.Set;

public final class UploadData {

    public final Set<Library> libraries;
    public final Set<Book> books;
    public final long availableDays;

    public UploadData(Set<Library> libraries, Set<Book> books, long availableDays) {
        this.libraries = libraries;
        this.books = books;
        this.availableDays = availableDays;
    }

    @Override
    public String toString() {
        return "UploadData{" +
               "libraries=" + libraries +
               ", books=" + books +
               ", days=" + availableDays +
               '}';
    }
}
