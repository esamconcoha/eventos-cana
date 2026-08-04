package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.AdResponseUserDto;
import com.canabackend.cana.dtos.CrearUsuarioDto;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.models.Usuarios;
import com.canabackend.cana.projections.GetCatalogoByNombreProjection;
import com.canabackend.cana.repositories.CatalogosCanaRepository;
import com.canabackend.cana.seguridad.AuthenticationRequest;
import com.canabackend.cana.seguridad.AuthenticationResponse;
import com.canabackend.cana.seguridad.JwtUtilService;
import com.canabackend.cana.seguridad.UserDetailsServiceImpl;
import com.canabackend.cana.services.impl.UsuariosSvcImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/publico")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CatalogosCanaRepository catalogosCanaRepository;

    @Autowired
    private UserDetailsServiceImpl usuarioDetailService;


    @Autowired
    private UsuariosSvcImpl usuarioServicio;


    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static Logger logger
            = Logger.getLogger(
            AuthController.class.getName());
    @Autowired
    private JwtUtilService jwtUtil;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> createToken(@RequestBody AuthenticationRequest request) {
        // Un mismo correo puede tener varios registros (el CRUD borra logicamente,
        // apagando estado_usuario). Regla: media vez ALGUNO este activo se sigue;
        // si el correo esta registrado pero ningun registro esta activo, se corta
        // ANTES de validar credenciales con una excepcion propia de "no activo".
        List<Usuarios> registrados = usuarioServicio.findAllByCorreo(request.getUsername());
        Usuarios usuarioActivo = registrados.stream()
                .filter(u -> Boolean.TRUE.equals(u.getEstadoUsuario()))
                .findFirst()
                .orElse(null);

        if (usuarioActivo == null) {
            // Registrado pero sin ningun activo => inactivo. Ni registrado => credenciales.
            if (!registrados.isEmpty()) {
                throw new MSCanaException(ErrorEnum.USUARIO_INACTIVO);
            }
            throw new MSCanaException(ErrorEnum.I_BAD_CREDENTIALS);
        }

        String contraseña = usuarioActivo.getContrasenia(); // la contraseña del registro activo
        if (contraseña != null && passwordEncoder.matches(request.getPassword(), contraseña)) { // si la contraseña es correcta
            try {
                logger.log(Level.INFO, "se ejecuta el metodo createToken antes de authenticate");
                UserDetails userDetails = usuarioDetailService.loadUserByUsername(request.getUsername());
                logger.log(Level.INFO, "se ejecuta el metodo createToken despues de authenticate");
                String jwt = jwtUtil.generateToken(userDetails);
                AdResponseUserDto adResponseUserDto =  getInfoUser(usuarioActivo);

                return new ResponseEntity<>(new AuthenticationResponse(
                        jwt, adResponseUserDto.getNombre(), adResponseUserDto.getDpi(),
                        adResponseUserDto.getRol(), adResponseUserDto.getCodigoRol(),
                        adResponseUserDto.getNombreRol()), HttpStatus.OK);

            } catch (BadCredentialsException e) {
                throw  new MSCanaException(ErrorEnum.I_BAD_CREDENTIALS);
            }
        }else{
            throw  new MSCanaException(ErrorEnum.I_BAD_CREDENTIALS);
        }
    }


    public AdResponseUserDto getInfoUser(Usuarios usuario){
        String nombre = usuario.getNombresUsuario() +" " +usuario.getApellidosUsuario();
        Long idRol = usuario.getRol();

        // El rol vive en catalogos_cana; se manda el codigo para decidir
        // permisos y el nombre para mostrar, y nunca el id crudo a la vista.
        String codigoRol = null;
        String nombreRol = null;
        if (idRol != null) {
            GetCatalogoByNombreProjection rol = catalogosCanaRepository.getCatalogoPorId(idRol);
            if (rol != null) {
                codigoRol = rol.getCodigo();
                nombreRol = rol.getNombre();
            }
        }
        return new AdResponseUserDto(nombre, usuario.getDpiNitUsuario(),
                idRol != null ? idRol.intValue() : null, codigoRol, nombreRol);
    }


    @PostMapping("/guardarUsuario")
    public Usuarios guardarUsuario(@RequestBody CrearUsuarioDto usuario){
        logger.log(Level.INFO, "Se ejecuta el metodo guardarUsuario");
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String passwordEncriptada = passwordEncoder.encode(usuario.getContrasenia());
        usuario.setContrasenia(passwordEncriptada);

        return usuarioServicio.save(usuario);
    }
}
