namespace java ecore2thrift.example


struct Post {
	 /* Title of the post. */ 1: required string title,
	 /* Date and time in ISO 8601 format. */ 2: required string date,
	 /* Text in the post. */ 3: required string text,
	 /* True if the post is a draft and should not be visible yet. */ 4: required bool draft,
}

exception PostDoesNotExist {
	 /* Unique identifier for the post. */ 1: required i32 identifier,
}

struct Blog {
	 /* List of posts in the blog. */ 1: required list<Post> posts,
}

/* Manages the contents of the blog. */
service BlogService {
  /* Returns the entire blog at once. Auth needed: Yes */
  Blog getBlog(
  )
	
  /* Returns the number of posts in the blog. Auth needed: Yes */
  i32 getCountPosts(
  )
	
  /* Returns a specific post. Auth needed: Yes */
  Post getPost(
	/*  */ 1: required i32 identifier,
  )
  throws (
	1: PostDoesNotExist err1 /* The specified post has not been found. */ 
	) 
	
  /* Removes a specific post. Auth needed: Yes */
  void removePost(
	/*  */ 1: required i32 identifier,
  )
  throws (
	1: PostDoesNotExist err1 /* The specified post has not been found. */ 
	) 
	
  /* Updates a specific post. Auth needed: Yes */
  void updatePost(
	/*  */ 1: required i32 identifier,
	/*  */ 2: required Post updatedPost,
  )
  throws (
	1: PostDoesNotExist err1 /* The specified post has not been found. */ 
	) 
	
  /* Adds a new post. Auth needed: Yes */
  i32 addPost(
	/*  */ 1: required Post newPost,
  )
	
}

