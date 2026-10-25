from sqlalchemy.orm import Session

from app.core.exceptions import NotFoundException
from app.models.cliente import Cliente


def get_cliente(db: Session, cliente_id: str, taller_id: str) -> Cliente:
    cliente = db.query(Cliente).filter(
        Cliente.id == cliente_id,
        Cliente.id_taller == taller_id,
    ).first()
    if not cliente:
        raise NotFoundException("Cliente")
    return cliente
