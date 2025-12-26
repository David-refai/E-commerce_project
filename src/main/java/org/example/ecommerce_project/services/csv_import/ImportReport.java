package org.example.ecommerce_project.services.csv_import;

import org.example.ecommerce_project.exception.ImportError;

import java.util.ArrayList;
import java.util.List;

public class ImportReport {
    private int total;
    private int success;
    private final List<ImportError> errors = new ArrayList<>();

    public void incTotal() { total++; }
    public void incSuccess() { success++; }
    public void addError(int row, String msg, String raw) { errors.add(new ImportError(row, msg, raw)); }

    public int total() { return total; }
    public int success() { return success; }
    public int failed() { return errors.size(); }
    public List<ImportError> errors() { return errors; }
}

 