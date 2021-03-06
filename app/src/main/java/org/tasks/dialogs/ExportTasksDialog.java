package org.tasks.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import org.tasks.backup.TasksJsonExporter;

@AndroidEntryPoint
public class ExportTasksDialog extends DialogFragment {

  @Inject DialogBuilder dialogBuilder;
  @Inject TasksJsonExporter tasksJsonExporter;

  public static ExportTasksDialog newExportTasksDialog() {
    return new ExportTasksDialog();
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    ProgressDialog progressDialog = dialogBuilder.newProgressDialog();
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setProgress(0);
    progressDialog.setCancelable(false);
    progressDialog.setIndeterminate(false);
    progressDialog.show();

    setCancelable(false);
    tasksJsonExporter.exportTasks(
        getActivity(), TasksJsonExporter.ExportType.EXPORT_TYPE_MANUAL, progressDialog);
    return progressDialog;
  }
}
