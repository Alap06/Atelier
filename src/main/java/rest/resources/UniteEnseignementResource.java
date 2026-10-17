package rest.resources;

import entities.UniteEnseignement;
import filtres.Secured;
import services.UniteEnseignementService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/UE")
public class UniteEnseignementResource {

    private final UniteEnseignementService service = new UniteEnseignementService();

    @Secured
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUE(UniteEnseignement ue) {
        if (!service.create(ue))
            return Response.status(Response.Status.NOT_FOUND).entity("Code déjà existant.").build();
        return Response.status(Response.Status.CREATED).entity(ue).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUE(@QueryParam("semestre") @DefaultValue("-1") int semestre,
                          @QueryParam("code")     @DefaultValue("-1") int code) {
        if (code != -1) {
            UniteEnseignement ue = service.findByCode(code);
            return ue == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(ue).build();
        }
        if (semestre != -1) {
            List<UniteEnseignement> r = service.findBySemestre(semestre);
            return r.isEmpty() ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(r).build();
        }
        List<UniteEnseignement> all = service.findAll();
        return all.isEmpty() ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(all).build();
    }

    @DELETE
    @Path("/{code}")
    public Response deleteUE(@PathParam("code") int code) {
        return service.delete(code) ? Response.ok("UE " + code + " supprimée.").build()
                                    : Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("/{code}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUE(@PathParam("code") int code, UniteEnseignement updated) {
        UniteEnseignement r = service.update(code, updated);
        return r == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(r).build();
    }
}
