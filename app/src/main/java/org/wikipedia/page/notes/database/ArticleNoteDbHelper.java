package org.wikipedia.page.notes.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import org.wikipedia.WikipediaApp;
import org.wikipedia.database.contract.ArticleContract;
import org.wikipedia.database.contract.ArticleNoteContract;
import org.wikipedia.page.notes.Article;
import org.wikipedia.page.notes.Note;
import org.wikipedia.util.log.L;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amawai on 28/03/18.
 */

public class ArticleNoteDbHelper {
    private static ArticleNoteDbHelper INSTANCE;

    public static ArticleNoteDbHelper instance() {
        if (INSTANCE == null) {
            INSTANCE = new ArticleNoteDbHelper();
        }
        return INSTANCE;
    }

    public List<Article> getAllArticles() {
        List<Article> articleList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(ArticleContract.TABLE, null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                Article article = Article.DATABASE_TABLE.fromCursor(cursor);
                articleList.add(article);
            }
        }
        for (Article list : articleList) {
            Log.d("ARTICLE_HELPER", "title: " + list.getArticleTitle() + " scroll : " + list.getScrollPosition());
        }
        return articleList;
    }

    public int getScrollOfArticle(Article article) {
        SQLiteDatabase db = getReadableDatabase();
        int scrollPosition = 0;
        try (Cursor cursor = db.query(ArticleContract.TABLE, null, ArticleContract.Col.ARTICLE_TITLE.getName() + " = ?",
                new String[]{article.getArticleTitle()},
                null, null, null)) {
            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                scrollPosition = Article.DATABASE_TABLE.fromCursor(cursor).getScrollPosition();
            }
        }
        return scrollPosition;
    }

    @NonNull
    public Article createArticle(@NonNull String title, @NonNull int scroll) {
        SQLiteDatabase db = getWritableDatabase();
        return createArticle(db, title, scroll);
    }

    @NonNull
    public Article createArticle(@NonNull SQLiteDatabase db, @NonNull String title, @NonNull int scroll) {
            db.beginTransaction();
            try {
                if (!articleExistsInDb(getReadableDatabase(), title)) {
                    Article createdArticle = new Article(title, scroll);
                    long id = db.insertOrThrow(ArticleContract.TABLE, null,
                            Article.DATABASE_TABLE.toContentValues(createdArticle));
                    db.setTransactionSuccessful();
                    createdArticle.setId(id);
                    return createdArticle;
                }
            } finally {
                db.endTransaction();
            }
        return null;
    }

    public Article getArticleByTitle(String title) {
        Article article = null;
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(ArticleContract.TABLE, null,
                ArticleContract.Col.ARTICLE_TITLE.getName() + " = ?", new String[]{title},
                null, null,null)) {
            if (cursor.moveToFirst()) {
                article = Article.DATABASE_TABLE.fromCursor(cursor);
            }
        }
        return article;
    }

    public void updateScrollState(@NonNull Article article) {
        SQLiteDatabase db = getWritableDatabase();
        updateScrollState(db, article);
    }

    public void updateScrollState(@NonNull SQLiteDatabase db, @NonNull Article article) {
        db.beginTransaction();
        try {
            updateScrollInDb(db, article);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Note addNote(@NonNull Article article, String noteTitle, String noteDescription, int scrollPosition) {
        Note newNote = new Note(article.getId(), noteTitle, noteDescription, scrollPosition);
        return addNote(article, newNote);
    }

    public Note addNote(@NonNull Article article, @NonNull Note note) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            //addNote(db, article, note);
            //db.setTransactionSuccessful();
            long id = db.insertOrThrow(ArticleNoteContract.TABLE, null,
                    Note.DATABASE_TABLE.toContentValues(note));
            db.setTransactionSuccessful();
            note.setId(id);
            return note;
        } finally {
            db.endTransaction();
        }
    }

    public void addNotes(@NonNull Article article, @NonNull List<Note> notes) {
        SQLiteDatabase db = getWritableDatabase();
        addNotes(db, article, notes);
    }

    public void addNotes(@NonNull SQLiteDatabase db, @NonNull Article article, @NonNull List<Note> notes) {
        db.beginTransaction();
        try {
            for (Note note : notes) {
                insertNoteInDb(db, article, note);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    public void updateNote(@NonNull Article article, String noteTitle, String noteDescription, int scrollPosition) {
        Note updatedNote = new Note(article.getId(), noteTitle, noteDescription, scrollPosition);
        updateNote(updatedNote);
    }

    public void updateNote(@NonNull Note note) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            updateNoteInDb(db, note);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    public void deleteNote(@NonNull Note note) {
        deleteNoteFromDb(getReadableDatabase(), note);
    }

    public List<Note> getNotesFromArticle(@NonNull Article article) {
        SQLiteDatabase db = getReadableDatabase();
        return getNotesFromArticle(db, article);
    }


    public List<Note> getNotesFromArticle(@NonNull SQLiteDatabase db, @NonNull Article article) {
        List<Note> noteList = new ArrayList<>();
        try (Cursor cursor = db.query(ArticleNoteContract.TABLE, null,
                ArticleNoteContract.Col.ARTICLE_ID.getName() + " = ?", new String[]{Long.toString(article.getId())},
                null, null,null)) {
            while (cursor.moveToNext()) {
                Note note = Note.DATABASE_TABLE.fromCursor(cursor);
                noteList.add(note);
            }
        }
        for (Note list : noteList) {
            Log.d("ARTICLE_HELPER", "title: " + list.getNoteTitle() + " scroll : " + list.getScrollPosition());
        }
        Log.d("ARTICLE_HELPER", "size is " + noteList.size());
        return noteList;
    }

    public void deleteAllNotesFromArticle(@NonNull Article article) {
        List<Note> notesToDelete = getNotesFromArticle(article);
        for (Note note : notesToDelete) {
            deleteNote(note);
        }
    }

    private void addNote(SQLiteDatabase db, @NonNull Article article, @NonNull Note note) {
        insertNoteInDb(db, article, note);
    }

    private void insertNoteInDb(SQLiteDatabase db, @NonNull Article article, @NonNull Note note) {
        note.setArticleId(article.getId());
        long id = db.insertOrThrow(ArticleNoteContract.TABLE, null,
                Note.DATABASE_TABLE.toContentValues(note));
        note.setId(id);
    }

    private void updateScrollInDb(SQLiteDatabase db, @NonNull Article article) {
        int result = db.update(ArticleContract.TABLE, article.DATABASE_TABLE.toContentValues(article),
                ArticleContract.Col.ID.getName() + " = ?", new String[]{Long.toString(article.getId())});
        if (result != 1) {
            L.w("Failed to update scroll position for article " + article.getArticleTitle() + " at scroll " + article.getScrollPosition());
        }
    }

    private void updateNoteInDb(SQLiteDatabase db, @NonNull Note note) {
        int result = db.update(ArticleNoteContract.TABLE, Note.DATABASE_TABLE.toContentValues(note),
                ArticleNoteContract.Col.ID.getName() + " = ?", new String[]{Long.toString(note.getNoteId())});
        if (result != 1) {
            L.w("Failed to update db entry for page " + note.getNoteTitle());
        }
    }

    private void deleteNoteFromDb(SQLiteDatabase db, @NonNull Note note) {
        int result = db.delete(ArticleNoteContract.TABLE,
                ArticleNoteContract.Col.ID.getName() + " = ?",
                new String[]{Long.toString(note.getNoteId())});
        if (result != 1) {
            L.w("Failed to delete db entry for page " + note.getNoteTitle());
        }
    }

    //This method checks if article currently exists in the database
    private boolean articleExistsInDb(SQLiteDatabase db, @NonNull String title) {
        try (Cursor cursor = db.query(ArticleContract.TABLE, null,
                ArticleContract.Col.ARTICLE_TITLE.getName() + " = ?",
                new String[]{title},
                null, null, null)) {
            if (cursor.getCount() > 0) {
                return true;
            }
        }
        return false;
    }

    private SQLiteDatabase getReadableDatabase() {
        return WikipediaApp.getInstance().getDatabase().getReadableDatabase();
    }

    private SQLiteDatabase getWritableDatabase() {
        return WikipediaApp.getInstance().getDatabase().getWritableDatabase();
    }
}
