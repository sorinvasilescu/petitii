using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;

namespace GovItHub.EmailClassifier.Models
{
    public class EmailClassifierContext : DbContext
    {
        public EmailClassifierContext(DbContextOptions<EmailClassifierContext> options)
            : base(options)
        { }

        public DbSet<EmailClasses> EmailClasses { get; set; }

        protected override void OnModelCreating(ModelBuilder builder)
        {
            builder.Entity<EmailClasses>().HasKey(m => m.EmailClassId);
            
            base.OnModelCreating(builder);
        }

    }
}
