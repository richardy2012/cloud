$(function() {
    $("span.expand-additional-metrics").click(function(){
        var status = window.localStorage.getItem("expand-additional-metrics") == "true";
        status = !status;

        // Expand the list of additional metrics.
        var additionalMetricsDiv = $(this).parent().find('.additional-metrics');
        $(additionalMetricsDiv).toggleClass('collapsed');

        // Switch the class of the arrow from open to closed.
        $(this).find('.expand-additional-metrics-arrow').toggleClass('arrow-open');
        $(this).find('.expand-additional-metrics-arrow').toggleClass('arrow-closed');

        window.localStorage.setItem("expand-additional-metrics", "" + status);
    });

    if (window.localStorage.getItem("expand-additional-metrics") == "true") {
        // Set it to false so that the click function can revert it
        window.localStorage.setItem("expand-additional-metrics", "false");
        $("span.expand-additional-metrics").trigger("click");
    }

    stripeSummaryTable();

    $('input[type="checkbox"]').click(function() {
        var name = $(this).attr("name")
        var column = "table ." + name;
        var status = window.localStorage.getItem(name) == "true";
        status = !status;
        $(column).toggle();
        stripeSummaryTable();
        window.localStorage.setItem(name, "" + status);
    });

    $("#select-all-metrics").click(function() {
       var status = window.localStorage.getItem("select-all-metrics") == "true";
       status = !status;
       if (this.checked) {
          // Toggle all un-checked options.
          $('input[type="checkbox"]:not(:checked)').trigger('click');
       } else {
          // Toggle all checked options.
          $('input[type="checkbox"]:checked').trigger('click');
       }
       window.localStorage.setItem("select-all-metrics", "" + status);
    });

    if (window.localStorage.getItem("select-all-metrics") == "true") {
        $("#select-all-metrics").attr('checked', status);
    }

    $("span.additional-metric-title").parent().find('input[type="checkbox"]').each(function() {
        var name = $(this).attr("name")
        // If name is undefined, then skip it because it's the "select-all-metrics" checkbox
        if (name && window.localStorage.getItem(name) == "true") {
            // Set it to false so that the click function can revert it
            window.localStorage.setItem(name, "false");
            $(this).trigger("click")
        }
    });

    // Trigger a click on the checkbox if a user clicks the label next to it.
    $("span.additional-metric-title").click(function() {
        $(this).parent().find('input[type="checkbox"]').trigger('click');
    });

    // Trigger a double click on the span to show full job description.
    $(".description-input").dblclick(function() {
        $(this).removeClass("description-input").addClass("description-input-full");
    });
});
