package br.com.entra21.conexaoretalho.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.entra21.conexaoretalho.models.Empresa;
import br.com.entra21.conexaoretalho.models.Endereco;
import br.com.entra21.conexaoretalho.models.Instituicao;
import br.com.entra21.conexaoretalho.models.Ong;
import br.com.entra21.conexaoretalho.models.Responsavel;
import br.com.entra21.conexaoretalho.models.Retalho;
import br.com.entra21.conexaoretalho.models.Role;
import br.com.entra21.conexaoretalho.models.Usuario;
import br.com.entra21.conexaoretalho.repository.EmpresaRepository;
import br.com.entra21.conexaoretalho.repository.EnderecoRepository;
import br.com.entra21.conexaoretalho.repository.InstituicaoRepository;
import br.com.entra21.conexaoretalho.repository.OngRepository;
import br.com.entra21.conexaoretalho.repository.ProdutoRepository;
import br.com.entra21.conexaoretalho.repository.ResponsavelRepository;
import br.com.entra21.conexaoretalho.repository.RetalhoRepository;
import br.com.entra21.conexaoretalho.repository.RoleRepository;
import br.com.entra21.conexaoretalho.repository.UsuarioRepository;

@Controller
public class InstituicaoController {

	// Beans

	@Autowired
	private EmpresaRepository empr;

	@Autowired
	private EnderecoRepository endr;

	@Autowired
	private InstituicaoRepository ir;

	@Autowired
	private OngRepository or;

	@Autowired
	private ProdutoRepository pr;

	@Autowired
	private ResponsavelRepository respr;

	@Autowired
	private RetalhoRepository retr;

	@Autowired
	private UsuarioRepository ur;

	@Autowired
	private RoleRepository rr;

	// CADASTRO

	// CADASTRO DE EMPRESA
	@RequestMapping(value = "/cadastrar/empresa", method = RequestMethod.GET)
	public String cadastrarEmpresa(Model model) {
		model.addAttribute("usuario", new Usuario());
		return "instituicao/cadastrarEmpresa";
	}

	@RequestMapping(value = "/cadastrar/empresa", method = RequestMethod.POST)
	public String cadastrarEmpresaPost(Empresa empresa, Endereco endereco, Responsavel responsavel, Usuario user,
			BindingResult result, Model model, RedirectAttributes attributes) {

		Role role = rr.findByNomeRole("ROLE_USER_EMPR");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		user.setRoles(roles);
		user.setSenha(new BCryptPasswordEncoder().encode(user.getSenha()));
		user.setLogin(empresa.getCnpj());

		endereco.setInstituicao(empresa);
		responsavel.setInstituicao(empresa);

//		// VALIDATIONS
//		if (result.hasErrors()) {
//			attributes.addFlashAttribute("mensagem", "Campos nulos não são permitidos!");
//			return "redirect:/cadastrar/empresa";
//		}
//
//		Usuario usr = ur.findByLogin(user.getLogin());
//		if (usr != null) {
//			model.addAttribute("cnpjExiste", "CNPJ já cadastrado!");
//			return "redirect:/cadastrar/empresa";
//		}

		// SAVING INTO DATABASE
		ur.save(user);
		empr.save(empresa);
		endr.save(endereco);
		respr.save(responsavel);

		attributes.addFlashAttribute("mensagem", "Usuário salvo com sucesso!");
		return "redirect:/cadastrar/empresa";
	}

	// CADASTRO DA ONG
	@RequestMapping(value = "/cadastrar/ong", method = RequestMethod.GET)
	public String cadastrarOng(Model model) {
		model.addAttribute("usuario", new Usuario());
		return "instituicao/cadastrarOng";
	}

	@RequestMapping(value = "/cadastrar/ong", method = RequestMethod.POST)
	public String cadastrarOngPost(Ong ong, Endereco endereco, Responsavel responsavel, Usuario user) {

		Role role = rr.findByNomeRole("ROLE_USER_ONG");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		user.setRoles(roles);
		user.setSenha(new BCryptPasswordEncoder().encode(user.getSenha()));
		user.setLogin(ong.getCnpj());
		ur.save(user);

		endereco.setInstituicao(ong);
		responsavel.setInstituicao(ong);

		or.save(ong);
		endr.save(endereco);
		respr.save(responsavel);

		return "redirect:/cadastrar/ong";
	}

	// BUSCAR INSTITUIÇÕES E PEGA A LISTA DE INSTITUIÇÕES
	@RequestMapping(value = "/listaEmpresas", method = RequestMethod.GET)
	public ModelAndView listaEmpresas() {
		ModelAndView mv = new ModelAndView("instituicao/listaEmpresas");
		// PROCURA A LISTA DE INSTITUICOES
		Iterable<Instituicao> instituicoes = ir.findAll();
		mv.addObject("instituicoes", instituicoes); // instituicoes = atributo que esta no html

		return mv;
	}

	// PERFIS

	// PERFIL DA EMPRESA
	@RequestMapping(value = "/perfil/empresa", method = RequestMethod.GET)
	public ModelAndView perfilEmpresa(long cnpj) {

		String cnpjStr = Long.toString(cnpj);

		ModelAndView mv;

		

		if (empr.findByCnpj(cnpjStr) != null) {
			mv = new ModelAndView("instituicao/perfilEmpresa");
			Empresa empresa = empr.findByCnpj(cnpjStr);
			mv.addObject("instituicao", empresa);
			Iterable<Retalho> retalhos = retr.findByEmpresa(empresa);
			mv.addObject("lretalhos", retalhos);
			

		} else {
			mv = new ModelAndView("instituicao/perfilOng");
			Ong ong = or.findByCnpj(cnpjStr);
			mv.addObject("instituicao", ong);
			
		}
		
		Instituicao instituicao = ir.findByCnpj(cnpjStr);
		Iterable<Responsavel> responsaveis = respr.findByInstituicao(instituicao);
		mv.addObject("lresponsaveis", responsaveis);

		return mv;
	}

	// RETALHO

	// CADASTRAR RETALHO
	@RequestMapping(value = "/cadastrar-retalho", method = RequestMethod.GET)
	public ModelAndView cadastrarRetalho(long cnpj) {
		Empresa empresa = empr.findByCnpj(Long.toString(cnpj));
		ModelAndView mv = new ModelAndView("retalho/cadastrarRetalho");
		mv.addObject("empresa", empresa);

		return mv;
	}

	@RequestMapping(value = "/cadastrar-retalho", method = RequestMethod.POST)
	public String cadastrarRetalhoPost(long cnpj, Retalho retalho) {
		String codLong = Long.toString(cnpj);
		Empresa empresa = empr.findByCnpj(codLong);
		retalho.setEmpresa(empresa);
		
		List<Retalho> empRetalhos = empresa.getRetalhos();
		empRetalhos.add(retalho);
		empresa.setRetalhos(empRetalhos);
		
		System.out.println(retalho);
		System.out.println(empresa);
		
		retr.save(retalho);
		empr.save(empresa);
		
		return "redirect:/listaEmpresas";
	}

	@RequestMapping(value = "/descricao-retalho", method = RequestMethod.GET)
	public ModelAndView descricaoRetalho(String cnpj, long codigo) {
		ModelAndView mv = new ModelAndView("retalho/descricaoRetalho");
//		String codLong = Long.toString(cnpj);
		
		Retalho retalho = retr.findByCodigo(codigo);
		Empresa empresa = empr.findByCnpj(cnpj);
		
		mv.addObject("empresa", empresa);
		mv.addObject("retalho", retalho);
		
		return mv;

	}
	
	@RequestMapping(value = "/agendar-coleta", method = RequestMethod.GET)
	public ModelAndView agendarColeta(String cnpj, long codigo) {
		ModelAndView mv = new ModelAndView("retalho/agendarColeta");
		
		Retalho retalho = retr.findByCodigo(codigo);
		Empresa empresa = empr.findByCnpj(cnpj);
		
		mv.addObject("empresa", empresa);
		mv.addObject("retalho", retalho);
		
		return mv;
	}

	@RequestMapping(value = "/agendar-coleta", method = RequestMethod.POST)
	public String agendarColetaPost(String cnpj, long codigo) {
//		if (result.hasErrors()) {
//			attributes.addFlashAttribute("mensagem", "Agenda indisponivel, marque para outro dia!");
//			return "redirect:/agendarColeta";
//		}
//		ir.save(instituicao);
//		attributes.addFlashAttribute("mensagem", "Coleta cadastrada com sucesso!");
		
		
		
		return "redirect:/agendarColeta";
	}

	@RequestMapping(value = "/produtos/ongs", method = RequestMethod.GET)
	public String produtosOngs() {
		return "instituicao/produtosOngs";

	}

}
