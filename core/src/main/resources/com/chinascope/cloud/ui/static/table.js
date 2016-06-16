function stripeSummaryTable() {
    $("#task-summary-table").find("tr:not(:hidden)").each(function (index) {
       if (index % 2 == 1) {
         $(this).css("background-color", "#f9f9f9");
       } else {
         $(this).css("background-color", "#ffffff");
       }
    });
}
