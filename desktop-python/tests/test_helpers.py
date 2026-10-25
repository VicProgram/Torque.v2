from datetime import date

from app.utils.helpers import format_decimal, format_fecha, format_fecha_date


def test_format_fecha_iso():
    assert format_fecha("2024-06-15T10:30:00") == "15/06/2024"


def test_format_fecha_empty():
    assert format_fecha(None) == ""
    assert format_fecha("") == ""


def test_format_decimal():
    assert format_decimal(1234.50) == "1234.50 €"
    assert format_decimal(None) == ""
    assert format_decimal(0) == "0.00 €"


def test_format_fecha_date():
    d = date(2024, 12, 25)
    assert format_fecha_date(d) == "25/12/2024"
    assert format_fecha_date(None) == ""
