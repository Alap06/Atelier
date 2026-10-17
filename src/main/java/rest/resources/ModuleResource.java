package rest.resources;

import entities.Module;
import filtres.Secured;
import services.ModuleService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/modules")
public class ModuleResource {

    private final ModuleService service = new ModuleService();

    @Secured
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createModule(Module module) {
        if (!service.create(module))
            return Response.status(Response.Status.CONFLICT).entity("Module existe déjà.").build();
        return Response.status(Response.Status.CREATED).entity(module).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllModules() {
        List<Module> all = service.findAll();
        return all.isEmpty() ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(all).build();
    }

    @Secured
    @DELETE
    @Path("/{matricule}")
    public Response deleteModule(@PathParam("matricule") String matricule) {
        return service.delete(matricule) ? Response.ok("Module " + matricule + " supprimé.").build()
                                        : Response.status(Response.Status.NOT_FOUND).build();
    }
}
