package br.ufrn.dimap.transport;

import br.ufrn.dimap.types.HttpContext;

public interface IBeamDispatcher {
    void dispatch(HttpContext context);
}
