$(function () {
    $("#isNeedPartition").val("false")
    window.localStorage.setItem("expand-additional-metrics", "" + false);
    $("span.expand-additional-metrics").click(function () {
        var status = window.localStorage.getItem("expand-additional-metrics") == "true";
        status = !status;
        if (status == true) {
            // $("#partitionField").val("");
            $("#partitionNum").val("");
            $("#partitionBtn").removeClass("btn btn-warning").addClass("btn btn-success")
            $("#isNeedPartition").val("true")
        } else {
            $("#isNeedPartition").val("false")
            $("#partitionBtn").removeClass("btn btn-success").addClass("btn btn-warning")
        }
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
