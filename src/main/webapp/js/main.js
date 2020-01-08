/**
 * 非表示のコマンドボタンをクリックする
 * @param {type} id
 * @returns {undefined}
 */
function clickHiddenButton(id) {
    $("[id$='" + id + "']").click();
}
