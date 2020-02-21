package com.abcde;

public final class Book {
    public final long id;
    public final long score;

    public Book(long id, long score) {
        this.id = id;
        this.score = score;
    }

    @Override
    public String toString() {
        return "Book{" +
               "id=" + id +
               ", score=" + score +
               '}';
    }
}
