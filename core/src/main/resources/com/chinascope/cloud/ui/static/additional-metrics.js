$(function () {
    $("span.expand-additional-metrics").click(function () {
        var status = window.localStorage.getItem("expand-additional-metrics") == "true";
        if (status == false) {
            $("#partitionField").val("");
            $("#partitionNum").val("");
        }
        status = !status;
        var additionalMetricsDiv = $(this).parent().find('.additional-metrics');
        $(additionalMetricsDiv).toggleClass('collapsed');
        // Switch the class of the arrow from open to closed.
        $(this).find('.expand-additional-metrics-arrow').toggleClass('arrow-open');
        $(this).find('.expand-additional-metrics-arrow').toggleClass('arrow-closed');
        window.localStorage.setItem("expand-additional-metrics", "" + status);
    });

    $(".selectpicker").selectpicker({
        "selectedText": "cat"
    });


});
