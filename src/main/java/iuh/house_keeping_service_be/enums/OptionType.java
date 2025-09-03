package iuh.house_keeping_service_be.enums; // Hoặc package tương ứng của bạn

public enum OptionType {
    // Chọn một trong nhiều lựa chọn, hiển thị dạng radio button
    SINGLE_CHOICE_RADIO,

    // Chọn một trong nhiều lựa chọn, hiển thị dạng dropdown
    SINGLE_CHOICE_DROPDOWN,

    // Chọn nhiều lựa chọn, hiển thị dạng checkbox
    MULTIPLE_CHOICE_CHECKBOX,

    // Ô nhập số (ví dụ: số lượng, diện tích)
    QUANTITY_INPUT,

    // Ô nhập văn bản ngắn
    TEXT_INPUT
}