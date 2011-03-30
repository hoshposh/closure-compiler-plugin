/* -------------------------------------------------------- */
/* Miscellaneous Javascript Tools Library                   */
/* -------------------------------------------------------- */
/* Created:      2004-02-06 by Pierric PERMEZEL             */
/* -------------------------------------------------------- */
/* Copyright for these Javascript functions by              */
/* COSMOSBAY~VECTIS - http://www.cosmosbay-vectis.com       */
/* For permission to use this script please contact         */ 
/* Cosmosbay~Vectis at mailto:info@cosmosbay-vectis.com     */
/* -------------------------------------------------------- */


/* --------------------------------------------------
	Population automatique des propriétés d'un objet
*/
function addProps(obj, data, names, addNull) {
	for (var i = 0; i < names.length; i++) if(i < data.length || addNull) obj[names[i]] = data[i];
}


// ##########################################################################################
var SiteMapNode_argNames =
		[
			"label",			// libellé du noeud
			"url",				// URL de la page correspondant à ce noeud
			"target"			// cible
		];
function SiteMapNode() {
	// --- 1. Ajout des propriétés
	addProps(this, arguments, SiteMapNode_argNames, true);

	// --- 2. Initialisation des valeurs par défaut
	with(this) {
		// Propriétés
		this.parent = null;
		this.children = null;

		// Méthodes
		this.addChild = SiteMapNode_addChild;
		this.getParent = SiteMapNode_getParent;
		this.getChildren = SiteMapNode_getChildren;

		this.dumpAsHTML = SiteMapNode_dumpAsHTML;
	}
}

function SiteMapNode_addChild(child) {
	with(this) {
		if (child==null)
			return;

		if (children==null)
			this.children = new Array();

		children[children.length] = child;
		child.parent = this;
	}
}

function SiteMapNode_getParent() {
	with(this) {
		return this.parent;
	}
}

function SiteMapNode_getChildren() {
	with(this) {
		if (children==null)
			return new Array();
		else
			return children;
	}
}

function SiteMapNode_dumpAsHTML() {
	with(this) {
		var txt = '<a href="'+url+'">'+label+'</a> / '+url+' ('+target+')';

		if (children!=null) {
			txt+='<ul>';
			for(var i=0; i<children.length; i++)
				txt += '<li>'+children[i].dumpAsHTML()+'</li>';
			txt+='</ul>';
		}

		return txt;
	}
}






// ##########################################################################################
var SiteMap_argNames =
		[
			 "rootNode",
			 "pathToAventisRessources",
			 "conf"				// objet de configuration Aventis
/*			,"compareURL"		// comparateur d'URL à utiliser    */
		];

function SiteMap() {
	// --- 1. Ajout des propriétés
	addProps(this, arguments, SiteMap_argNames, true);

	// --- 2. Initialisation des valeurs par défaut
	with(this) {
		// Propriétés
		this.nodesHashtable = new Array();
		if (pathToAventisRessources==null)
			pathToAventisRessources = "aventis/";

		// Méthodes
		this.buildFromMenuItems = SiteMap_buildFromMenuItems;
		this.getStackMenuForCurrentPage = SiteMap_getStackMenuForCurrentPage;
		this.getBreadCrumbsForCurrentPage = SiteMap_getBreadCrumbsForCurrentPage;
		this.getPulldownMenuBar = SiteMap_getPulldownMenuBar;
		this.getCurrentPageNode = SiteMap_getCurrentPageNode;
	}
}






function SiteMap_buildFromMenuItems(menuitems) {
	with(this) {
		nodesHashtable[rootNode.url] = rootNode;

		var fURLparams = conf.URLparams.length > 0;

		var curParentNode = rootNode;
		var previousNode = rootNode;
		var curLevel = 0;

		for (var index=0; index<menuitems.length; index++) {
			var aItem = menuitems[index];

			// Get node properties: 0=level, 1=caption, 2=link, 3=target
			var newLevel   = aItem[0];
			var newCaption = aItem[1];
			var newLink    = aItem[2];
			var hashKey = newLink;

			//alert(newLevel+" ("+curLevel+") / "+newCaption);

			var newTarget;
			switch(aItem[3]) {
				case 1:
					newTarget="_blank";
					break;

				case "":
				case null:
				case 0:
					newTarget="_self";
					break;

				default:
					newTarget = aItem[3];
			}

			// Ajout des params d'URL éventuels
			if (fURLparams && newLink!=null) {
				if (newLink.indexOf("?") == -1)
					newLink+="?"+conf.URLparams;
				else
					newLink+=conf.URLparams;
			}

			// Build node
			var newNode = new SiteMapNode(newCaption, newLink, newTarget);
			nodesHashtable[hashKey] = newNode;

			// Récupération du parent
			if (newLevel < curLevel) {
				//alert("<");
				// on remonte
				for(var i=0; i<curLevel-newLevel; i++) {
					curParentNode = curParentNode.getParent();
				}
			}
			else if (newLevel > curLevel) {
				//alert(">");
				// On descend de 1 niveau: le dernier noeud inséré devient le parent
				newLevel = curLevel+1;
				curParentNode = previousNode;
			}

			// Attachement du noeud à son père
			//alert(curParentNode);
			curParentNode.addChild(newNode);

			// et on reboucle !
			previousNode = newNode;
			curLevel = newLevel;
		}
	}
}


function SiteMap_getCurrentPageNode() {
	with(this) {
		var loc = window.location;
		var server = loc.protocol+"//"+loc.host;

		// --- tests par URL complete
		var currentNode = nodesHashtable[loc.href];						// href seul
		if (currentNode!=null)
			return currentNode;

		currentNode = nodesHashtable[server+loc.pathname];				// href sans paramètres ni ancre
		if (currentNode!=null)
			return currentNode;

		currentNode = nodesHashtable[server+loc.pathname+loc.search];	// href sans ancre
		if (currentNode!=null)
			return currentNode;

		currentNode = nodesHashtable[server+loc.pathname+loc.hash];		// href sans parametres
		if (currentNode!=null)
			return currentNode;

		// --- tests par URL relative à la racine du site
		currentNode = nodesHashtable["/"+loc.pathname];				// chemin absolu sans paramètres ni ancre
		if (currentNode!=null)
			return currentNode;

		currentNode = nodesHashtable["/"+loc.pathname+loc.search];	// chemin absolu sans ancre
		if (currentNode!=null)
			return currentNode;

		currentNode = nodesHashtable["/"+loc.pathname+loc.hash];		// chemin absolusans parametres
		if (currentNode!=null)
			return currentNode;

		// --- tests par URL relative au répertoire courant
		currentNode = nodesHashtable[loc.pathname];				// chemin relatif sans paramètres ni ancre
		if (currentNode!=null)
			return currentNode;

		currentNode = nodesHashtable[loc.pathname+loc.search];	// chemin relatif sans ancre
		if (currentNode!=null)
			return currentNode;

		currentNode = nodesHashtable[loc.pathname+loc.hash];		// chemin relatif sans parametres
		if (currentNode!=null)
			return currentNode;

		// --- tests spécifique aux pages par défaut
		var nomPages = ["index.htm", "index.html", "index.jsp", "index.asp", "index.aspx", "default.htm", "default.html", "default.jsp", "default.asp", "default.aspx" ];
		var loc2 = (loc.pathname.charAt(loc.pathname.length-1)=='/') ? loc.pathname: loc.pathname+"/";
		for(var i=0; i<nomPages.length; i++) {
			var nomPage = nomPages[i];

			currentNode = nodesHashtable[server+loc2+nomPage];		// chemin relatif sans paramètres ni ancre
			if (currentNode!=null)
				return currentNode;
	
			currentNode = nodesHashtable[server+loc2+nomPage+loc.search];	// chemin relatif sans ancre
			if (currentNode!=null)
				return currentNode;
	
			currentNode = nodesHashtable[server+loc2+nomPage+loc.hash];		// chemin relatif sans parametres
			if (currentNode!=null)
				return currentNode;
				
			currentNode = nodesHashtable[server+loc2+nomPage+loc.hash+loc.search];		// chemin relatif sans parametres
			if (currentNode!=null)
				return currentNode;
				
			currentNode = nodesHashtable[loc2+nomPage];				// chemin relatif sans paramètres ni ancre
			if (currentNode!=null)
				return currentNode;
	
			currentNode = nodesHashtable[loc2+nomPage+loc.search];	// chemin relatif sans ancre
			if (currentNode!=null)
				return currentNode;
	
			currentNode = nodesHashtable[loc2+nomPage+loc.hash];		// chemin relatif sans parametres
			if (currentNode!=null)
				return currentNode;

			currentNode = nodesHashtable[loc2+nomPage+loc.hash+loc.search];		// chemin relatif sans parametres
			if (currentNode!=null)
				return currentNode;
		}

		// PAS TROUVE !
		return null;
	}
}


function escapeQuotes(s) {
	if (s==null)
		return null;

	return s.replace(/'/i, "\\'");
}


function SiteMap_getBreadCrumbsForCurrentPage() {
	with(this) {
		// 1. Récupèration du noeud correspondant à l'URL de la page courante par simplification successive de son URL
		var currentNode = getCurrentPageNode();
		if (currentNode==null)
			return "<code>No node found for current url: "+window.location.href+"</code>";

		var tmp = '<span class="recentPageBread">'+currentNode.label+'</span>';

		var parentNode = currentNode.getParent();		
		while(parentNode!=null) {
			tmp = '<a href="'+parentNode.url+'" target="'+parentNode.target+'">'+parentNode.label+'</a> &gt; ' + tmp;
			parentNode = parentNode.getParent();
		}

		return tmp;
	}
}


function SiteMap_getStackMenuForCurrentPage() {
	with(this) {
		// 1. Récupèration du noeud correspondant à l'URL de la page courante par simplification successive de son URL
		var currentNode = getCurrentPageNode();
		if (currentNode==null)
			return "<code>No node found for current url: "+window.location.href+"</code>";

		var hasChildren = (currentNode.children!=null);

		// 2. Création du code HTML
		var txt = '<table border="0" cellpadding="0" cellspacing="0" class="stackMenu" width="100%"><colgroup><col width="1" /></colgroup><tbody>';

		// Niveaux parents
		var baseParentNode = hasChildren ? currentNode: currentNode.getParent();
		var parent = baseParentNode;
		var pTxt = "";

		while(parent!=null) {
			var onclick = 'goToURL(\''+escapeQuotes(parent.url)+'\',\''+parent.target+'\'); return true;';
			var onmouseover = 'window.status = \''+escapeQuotes(parent.label)+'\'; return true;';

			if ((!hasChildren && parent==currentNode.getParent()) || (parent==currentNode)) {
				// Père d'un noeud n'ayant pas de fils ==> sélectionné
				pTxt = '<tr onmouseover="'+onmouseover+'" onclick="'+onclick+'"><td class="currentNode" valign="top"><img src="'+pathToAventisRessources+'images/aventis/icons/funct/stackmenu_current.gif" border="0" alt="" /></td><td class="horizontalSpacer"></td><td class="currentLabel" valign="top">'+parent.label+'</td></tr><tr><td class="verticalSpacer" colspan="3"></td></tr>' + pTxt;
			}
			else {
				pTxt = '<tr onmouseover="'+onmouseover+'" onclick="'+onclick+'"><td class="parentNode" valign="top"><img src="'+pathToAventisRessources+'images/aventis/icons/funct/stackmenu_up.gif" border="0" alt="" /></td><td class="horizontalSpacer"></td><td class="parentLabel" valign="top">'+parent.label+'</td></tr><tr><td class="verticalSpacer" colspan="3"></td></tr>' + pTxt;
			}

			parent = parent.getParent();
		}

		txt += pTxt;

		// Niveau courant parmi les fils
		var cTxt = "";
		var children = baseParentNode.getChildren();

		for(var i=0; i<children.length; i++) {
			var child = children[i];

			var onclick = 'goToURL(\''+escapeQuotes(child.url)+'\',\''+child.target+'\'); return true;';
			var onmouseover = 'window.status = \''+escapeQuotes(child.label)+'\'; return true;';

			cTxt = '<tr onmouseover="'+onmouseover+'" onclick="'+onclick+'">';

			if (child==currentNode) {
				cTxt += '<td class="currentChildNode" valign="top"><img src="'+pathToAventisRessources+'images/aventis/icons/funct/stackmenu_currentWithNoChild.gif" border="0" alt="" /></td><td class="horizontalSpacer"></td><td class="currentChildLabel" valign="top">'+child.label+'</td>';
			}
			else {
				cTxt += '<td class="childNode" valign="top">&nbsp;</td><td class="horizontalSpacer"></td><td class="childLabel" valign="top">'+child.label+'</td>';
			}

			cTxt += '</tr>';

			txt += cTxt;
		}

		txt += '</tbody></table>';

		return txt;

	}
}


function SiteMap_getPulldownMenuBar() {
	with(this) {
		var txt = '';
		txt += '<table border="0" cellspacing="0" cellpadding="0" class="tbHead"><tr>';
		
		// Tous les fils du noeud racine
		var children = rootNode.getChildren();
		for(var i=0; i<children.length; i++) {
			var child = children[i];
			var menuHasItems = (child.getChildren().length > 0);
			
			var idList = "L1_"+(i+1);
			var idMenu = idList+"_MENU";
			var onclick = 'goToURL(\''+escapeQuotes(child.url)+'\',\''+child.target+'\'); return true;';
			var onmouseover = 'window.status = \''+escapeQuotes(child.label)+'\'; menOverTR(this.id,\''+idList+'\',3); this.style.cursor=\'hand\'; return true;';
			var onmouseout = 'this.style.cursor=\'default\'; return true;';

			var txt2 = '';
			
			if (i>0) {
				txt2 += '<td class="spacer">&nbsp;</td>';
			}
			
			txt2 += '<td><table border="0" cellspacing="0" cellpadding="0">';
			txt2 += '<tr class="itemlink" id="'+idMenu+'" onclick="'+onclick+'" onmouseover="'+onmouseover+'" onmouseout="'+onmouseout+'" >';
			txt2 += '<td width="5">&nbsp;</td><td>';
			
			if(menuHasItems) {
			    txt2 += '<img src="'+pathToAventisRessources+'images/aventis/icons/funct/menu_down.gif" border="0" />';
			} else {
			    txt2 += '<img src="'+pathToAventisRessources+'images/aventis/icons/funct/menu_right.gif" border="0" />';
			}
			
			txt2 += '</td>';
			txt2 += '<td nowrap class="tdHead">' + child.label + '</td>';
			txt2 += '</tr></table></td>';
			
			txt += txt2;
		}
		
		txt += '</tr></table>';
		
		return txt;
	}
}

